package com.sonarsource.cognitivecomplexity.project_explorer.domain;

public interface HasMeasures {

  void setLinesOfCode(long linesOfCode);

  void setCyclomaticComplexity(long cyclomaticComplexity);

  void setCognitiveComplexity(long cognitiveComplexity);

}