import 'dart:convert';
import 'package:flutter/material.dart';
import 'api.dart';

void main() {
  runApp(const JqaApp());
}

class JqaApp extends StatefulWidget {
  const JqaApp({super.key});

  @override
  State<JqaApp> createState() => _JqaAppState();
}

class _JqaAppState extends State<JqaApp> {
  late ApiClient api;
  String baseUrl = '';
  String username = 'alice';
  String password = 'secret';
  final _questionsKey = GlobalKey<_QuestionsListState>();

  @override
  void initState() {
    super.initState();
    api = ApiClient(baseUrl: baseUrl);
    _applyAuth();
  }

  void _applyAuth() {
    final raw = '$username:$password';
    final b64 = base64Encode(utf8.encode(raw));
    api.basicAuth = 'Basic $b64';
  }

  void _updateBaseUrl(String v) {
    setState(() {
      baseUrl = v;
      api.baseUrl = v;
    });
    _questionsKey.currentState?.reload();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'JQA',
      theme: ThemeData(useMaterial3: true, colorSchemeSeed: Colors.indigo),
      home: Builder(
        builder: (context) => Scaffold(
          appBar: AppBar(
            title: const Text('JQA: Questions & Answers'),
            actions: [
              IconButton(
                icon: const Icon(Icons.refresh),
                onPressed: () => _questionsKey.currentState?.reload(),
                tooltip: 'Refresh',
              )
            ],
          ),
          body: Column(
            children: [
              _TopBar(
                baseUrl: baseUrl,
                username: username,
                password: password,
                onBaseUrlChanged: _updateBaseUrl,
                onCredsChanged: (u, p) {
                  setState(() {
                    username = u;
                    password = p;
                    _applyAuth();
                  });
                  _questionsKey.currentState?.reload();
                },
              ),
              Expanded(
                child: QuestionsList(
                  key: _questionsKey,
                  api: api,
                  onOpen: (id) => _openDetails(context, id),
                ),
              ),
            ],
          ),
          floatingActionButton: FloatingActionButton(
            onPressed: () => _openCreateQuestion(context),
            child: const Icon(Icons.add),
          ),
        ),
      ),
    );
  }

  void _openCreateQuestion(BuildContext context) async {
    await Navigator.of(context).push(MaterialPageRoute(builder: (_) => CreateQuestionPage(api: api)));
    _questionsKey.currentState?.reload();
  }

  void _openDetails(BuildContext context, int id) async {
    await Navigator.of(context).push(MaterialPageRoute(builder: (_) => QuestionDetailsPage(api: api, questionId: id)));
    _questionsKey.currentState?.reload();
  }
}

class _TopBar extends StatefulWidget {
  final String baseUrl;
  final String username;
  final String password;
  final ValueChanged<String> onBaseUrlChanged;
  final void Function(String, String) onCredsChanged;
  const _TopBar({required this.baseUrl, required this.username, required this.password, required this.onBaseUrlChanged, required this.onCredsChanged});

  @override
  State<_TopBar> createState() => _TopBarState();
}

class _TopBarState extends State<_TopBar> {
  late final TextEditingController _base;
  late final TextEditingController _user;
  late final TextEditingController _pass;

  @override
  void initState() {
    super.initState();
    _base = TextEditingController(text: widget.baseUrl);
    _user = TextEditingController(text: widget.username);
    _pass = TextEditingController(text: widget.password);
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: Wrap(
        crossAxisAlignment: WrapCrossAlignment.center,
        spacing: 12,
        runSpacing: 8,
        children: [
          SizedBox(
            width: 280,
            child: TextField(
              controller: _base,
              decoration: const InputDecoration(labelText: 'Backend Base URL', hintText: 'http://localhost:8080'),
              onSubmitted: widget.onBaseUrlChanged,
            ),
          ),
          SizedBox(
            width: 160,
            child: TextField(
              controller: _user,
              decoration: const InputDecoration(labelText: 'Username'),
              onSubmitted: (_) => widget.onCredsChanged(_user.text, _pass.text),
            ),
          ),
          SizedBox(
            width: 160,
            child: TextField(
              controller: _pass,
              obscureText: true,
              decoration: const InputDecoration(labelText: 'Password'),
              onSubmitted: (_) => widget.onCredsChanged(_user.text, _pass.text),
            ),
          ),
          FilledButton(
            onPressed: () => widget.onBaseUrlChanged(_base.text),
            child: const Text('Apply URL'),
          ),
          OutlinedButton(
            onPressed: () => widget.onCredsChanged(_user.text, _pass.text),
            child: const Text('Apply Login'),
          ),
        ],
      ),
    );
  }
}

class QuestionsList extends StatefulWidget {
  final ApiClient api;
  final void Function(int id) onOpen;
  const QuestionsList({super.key, required this.api, required this.onOpen});

  @override
  State<QuestionsList> createState() => _QuestionsListState();
}

class _QuestionsListState extends State<QuestionsList> {
  late Future<List<dynamic>> _future;

  @override
  void initState() {
    super.initState();
    _future = widget.api.listQuestions();
  }

  void reload() {
    setState(() {
      _future = widget.api.listQuestions();
    });
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<List<dynamic>>(
      future: _future,
      builder: (context, snap) {
        if (snap.connectionState != ConnectionState.done) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snap.hasError) {
          return Center(child: Text('Error: ${snap.error}'));
        }
        final items = snap.data ?? [];
        if (items.isEmpty) {
          return const Center(child: Text('No questions yet. Create one with +'));
        }
        return ListView.separated(
          itemCount: items.length,
          separatorBuilder: (_, __) => const Divider(height: 1),
          itemBuilder: (context, index) {
            final q = items[index] as Map<String, dynamic>;
            return ListTile(
              title: Text(q['title'] ?? ''),
              subtitle: Text((q['content'] ?? '').toString(), maxLines: 2, overflow: TextOverflow.ellipsis),
              trailing: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  IconButton(icon: const Icon(Icons.thumb_up), onPressed: () async { await widget.api.voteQuestion(q['id'] as int, 'up'); setState(() { _future = widget.api.listQuestions(); }); }),
                  IconButton(icon: const Icon(Icons.thumb_down), onPressed: () async { await widget.api.voteQuestion(q['id'] as int, 'down'); setState(() { _future = widget.api.listQuestions(); }); }),
                ],
              ),
              onTap: () => widget.onOpen(q['id'] as int),
            );
          },
        );
      },
    );
  }
}

class CreateQuestionPage extends StatefulWidget {
  final ApiClient api;
  const CreateQuestionPage({super.key, required this.api});

  @override
  State<CreateQuestionPage> createState() => _CreateQuestionPageState();
}

class _CreateQuestionPageState extends State<CreateQuestionPage> {
  final _formKey = GlobalKey<FormState>();
  final _title = TextEditingController();
  final _content = TextEditingController();
  bool _busy = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('New Question')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              TextFormField(
                controller: _title,
                decoration: const InputDecoration(labelText: 'Title'),
                validator: (v) => (v == null || v.isEmpty) ? 'Enter a title' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _content,
                maxLines: 6,
                decoration: const InputDecoration(labelText: 'Content'),
                validator: (v) => (v == null || v.isEmpty) ? 'Enter content' : (v.length > 2000 ? 'Too long (max 2000)' : null),
              ),
              const SizedBox(height: 16),
              FilledButton.icon(
                onPressed: _busy ? null : () async {
                  if (!_formKey.currentState!.validate()) return;
                  setState(() { _busy = true; });
                  try {
                    await widget.api.createQuestion(_title.text.trim(), _content.text.trim());
                    if (mounted) Navigator.of(context).pop();
                  } catch (e) {
                    if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
                  } finally {
                    if (mounted) setState(() { _busy = false; });
                  }
                },
                icon: const Icon(Icons.save),
                label: const Text('Create'),
              )
            ],
          ),
        ),
      ),
    );
  }
}

class QuestionDetailsPage extends StatefulWidget {
  final ApiClient api;
  final int questionId;
  const QuestionDetailsPage({super.key, required this.api, required this.questionId});

  @override
  State<QuestionDetailsPage> createState() => _QuestionDetailsPageState();
}

class _QuestionDetailsPageState extends State<QuestionDetailsPage> {
  late Future<Map<String, dynamic>> _futureQ;
  late Future<List<dynamic>> _futureA;
  final _answerCtrl = TextEditingController();

  @override
  void initState() {
    super.initState();
    _futureQ = widget.api.getQuestionDetails(widget.questionId);
    _futureA = widget.api.listAnswers(widget.questionId);
  }

  Future<void> _reload() async {
    setState(() {
      _futureQ = widget.api.getQuestionDetails(widget.questionId);
      _futureA = widget.api.listAnswers(widget.questionId);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Question Details')),
      body: FutureBuilder<Map<String, dynamic>>(
        future: _futureQ,
        builder: (context, qSnap) {
          if (qSnap.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (qSnap.hasError) return Center(child: Text('Error: ${qSnap.error}'));
          final q = qSnap.data!;
          return Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(q['title'] ?? '', style: Theme.of(context).textTheme.headlineSmall),
                const SizedBox(height: 8),
                Text(q['content'] ?? ''),
                const SizedBox(height: 8),
                Wrap(spacing: 8, children: [
                  FilledButton.icon(onPressed: () async { await widget.api.voteQuestion(widget.questionId, 'up'); _reload(); }, icon: const Icon(Icons.thumb_up), label: const Text('Up')),
                  OutlinedButton.icon(onPressed: () async { await widget.api.voteQuestion(widget.questionId, 'down'); _reload(); }, icon: const Icon(Icons.thumb_down), label: const Text('Down')),
                ]),
                const Divider(height: 32),
                Text('Answers', style: Theme.of(context).textTheme.titleLarge),
                Expanded(
                  child: FutureBuilder<List<dynamic>>(
                    future: _futureA,
                    builder: (context, aSnap) {
                      if (aSnap.connectionState != ConnectionState.done) return const Center(child: CircularProgressIndicator());
                      if (aSnap.hasError) return Center(child: Text('Error: ${aSnap.error}'));
                      final answers = aSnap.data ?? [];
                      if (answers.isEmpty) return const Text('No answers yet.');
                      return ListView.separated(
                        itemCount: answers.length,
                        separatorBuilder: (_, __) => const Divider(height: 1),
                        itemBuilder: (context, i) {
                          final a = answers[i] as Map<String, dynamic>;
                          final isRight = a['right'] == true;
                          return ListTile(
                            leading: Icon(isRight ? Icons.check_circle : Icons.chat_bubble_outline, color: isRight ? Colors.green : null),
                            title: Text(a['content'] ?? ''),
                            subtitle: Text('by ${a['author'] ?? 'unknown'}'),
                            trailing: Wrap(spacing: 8, children: [
                              IconButton(icon: const Icon(Icons.thumb_up), onPressed: () async { await widget.api.voteAnswer(a['id'] as int, 'up'); _reload(); }),
                              IconButton(icon: const Icon(Icons.thumb_down), onPressed: () async { await widget.api.voteAnswer(a['id'] as int, 'down'); _reload(); }),
                              IconButton(icon: const Icon(Icons.verified), onPressed: () async { await widget.api.markRight(a['id'] as int); _reload(); }),
                            ]),
                          );
                        },
                      );
                    },
                  ),
                ),
                Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: _answerCtrl,
                        decoration: const InputDecoration(labelText: 'Write an answer...'),
                      ),
                    ),
                    const SizedBox(width: 8),
                    FilledButton(
                      onPressed: () async {
                        if (_answerCtrl.text.trim().isEmpty) return;
                        await widget.api.createAnswer(widget.questionId, _answerCtrl.text.trim());
                        _answerCtrl.clear();
                        _reload();
                      },
                      child: const Text('Post'),
                    )
                  ],
                )
              ],
            ),
          );
        },
      ),
    );
  }
}
