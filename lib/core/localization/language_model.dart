/// Metadata model representing a supported application language.
class AppLanguage {
  final String code;
  final String name;
  final String nativeName;
  final bool isRecommended;
  final String description;

  const AppLanguage({
    required this.code,
    required this.name,
    required this.nativeName,
    this.isRecommended = false,
    required this.description,
  });
}
