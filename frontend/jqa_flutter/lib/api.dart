import 'dart:convert';
import 'package:http/http.dart' as http;

class ApiClient {
  String baseUrl; // When empty, use relative URLs (good for web behind reverse proxy)
  String? basicAuth; // value of Authorization header e.g. Basic base64
  String? googleId;
  String? facebookId;

  ApiClient({required this.baseUrl});

  Map<String, String> _headers() {
    final h = <String, String>{'Content-Type': 'application/json'};
    if (basicAuth != null && basicAuth!.isNotEmpty) h['Authorization'] = basicAuth!;
    if (googleId != null && googleId!.isNotEmpty) h['X-Google-Id'] = googleId!;
    if (facebookId != null && facebookId!.isNotEmpty) h['X-Facebook-Id'] = facebookId!;
    return h;
  }

  Uri _u(String pathAndQuery) {
    if (baseUrl.isEmpty) return Uri.parse(pathAndQuery);
    return Uri.parse('$baseUrl$pathAndQuery');
  }

  Future<List<dynamic>> listQuestions({int page = 0, int size = 20}) async {
    final uri = _u('/api/questions?page=$page&size=$size');
    final resp = await http.get(uri, headers: _headers());
    if (resp.statusCode != 200) throw Exception('Failed to load questions: ${resp.statusCode}');
    final map = json.decode(resp.body) as Map<String, dynamic>;
    return map['content'] as List<dynamic>;
  }

  Future<Map<String, dynamic>> getQuestionDetails(int id) async {
    final resp = await http.get(_u('/api/questions/$id'), headers: _headers());
    if (resp.statusCode != 200) throw Exception('Question not found: ${resp.statusCode}');
    return json.decode(resp.body) as Map<String, dynamic>;
  }

  Future<void> createQuestion(String title, String content) async {
    final body = json.encode({'title': title, 'content': content});
    final resp = await http.post(_u('/api/questions'), headers: _headers(), body: body);
    if (resp.statusCode != 201) throw Exception('Failed to create question: ${resp.statusCode} ${resp.body}');
  }

  Future<List<dynamic>> listAnswers(int questionId) async {
    final resp = await http.get(_u('/api/questions/$questionId/answers'), headers: _headers());
    if (resp.statusCode != 200) throw Exception('Failed to load answers: ${resp.statusCode}');
    return json.decode(resp.body) as List<dynamic>;
  }

  Future<void> createAnswer(int questionId, String content) async {
    final body = json.encode({'content': content});
    final resp = await http.post(_u('/api/questions/$questionId/answers'), headers: _headers(), body: body);
    if (resp.statusCode != 201) throw Exception('Failed to create answer: ${resp.statusCode} ${resp.body}');
  }

  Future<void> voteQuestion(int id, String dir) async {
    final resp = await http.post(_u('/api/questions/$id/vote?dir=$dir'), headers: _headers());
    if (resp.statusCode != 204) throw Exception('Failed to vote: ${resp.statusCode}');
  }

  Future<void> voteAnswer(int id, String dir) async {
    final resp = await http.post(_u('/api/answers/$id/vote?dir=$dir'), headers: _headers());
    if (resp.statusCode != 204) throw Exception('Failed to vote: ${resp.statusCode}');
  }

  Future<void> markRight(int answerId) async {
    final resp = await http.post(_u('/api/answers/$answerId/mark-right'), headers: _headers());
    if (resp.statusCode != 200) throw Exception('Failed to mark right: ${resp.statusCode}');
  }
}
