package com.sonarsource.cognitivecomplexity.project_explorer.domain;

public class ProjectFile implements HasMeasures{

  String key;
  String language;

  long linesOfCode = -1;
  long cyclomaticComplexity = -1;
  long cognitiveComplexity = -1;


  public ProjectFile(String key, String language) {
    this.key = key;
    this.language = language;
  }

  public String getKey() {
    return key;
  }

  public String getLanguage() {
    return language;
  }

  public long getLinesOfCode() {
    return linesOfCode;
  }

  public long getCyclomaticComplexity() {
    return cyclomaticComplexity;
  }

  public long getCognitiveComplexity() {
    return cognitiveComplexity;
  }

  @Override
  public void setLinesOfCode(long linesOfCode) {
    this.linesOfCode = linesOfCode;
  }

  @Override
  public void setCyclomaticComplexity(long cyclomaticComplexity) {
    this.cyclomaticComplexity = cyclomaticComplexity;
  }

  @Override
  public void setCognitiveComplexity(long cognitiveComplexity) {
    this.cognitiveComplexity = cognitiveComplexity;
  }

  public boolean isValid() {
    return linesOfCode > 0 && cyclomaticComplexity > 0;
  }

  public String toString(){
    StringBuilder sb = new StringBuilder();

    String spacing = "\t";

    sb.append(key)
            .append(spacing).append("language: ").append(language)
            .append(spacing).append("LoC: ").append(linesOfCode)
            .append(spacing).append("Cyc: ").append(cyclomaticComplexity)
            .append(spacing).append("Cog: ").append(cognitiveComplexity)
            .append("\n");

    return sb.toString();
  }
}