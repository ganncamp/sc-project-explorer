package com.sonarsource.cognitivecomplexity.project_explorer.domain;

import java.util.ArrayList;
import java.util.List;

public class Project implements HasMeasures, Comparable<Project>{

  String uuid;
  String organization;
  String key;


  List<ProjectFile> files = new ArrayList<>();

  //metrics
  long linesOfCode;
  long cyclomaticComplexity;
  long cognitiveComplexity;
  long unresolvedIssues = 0;
  long fixedIssues = 0;
  long fpIssues = 0;
  long wfIssues = 0;

  List<Issue> issues = new ArrayList<>();

  public Project(String organization, String key, String uuid) {
    this(key, uuid);
    this.organization = organization;
  }

  public Project (String key, String uuid){
    this.uuid = uuid;
    this.key = key;
  }

  public void addIssue(Issue issue) {
    issues.add(issue);
    switch (issue.resolution) {
      case FIXED:
        fixedIssues++;
        break;
      case FALSEPOSITIVE:
        fpIssues++;
        break;
      case WONTFIX:
        wfIssues++;
        break;
      case UNRESOLVED:
        unresolvedIssues++;
        break;
    }
  }

  public void addProjectFile(ProjectFile projectFile) {
    this.files.add(projectFile);
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

  public String getOrganization() {
    return organization;
  }

  public String getKey() {
    return key;
  }

  public String getUuid() {
    return uuid;
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

  public long getUnresolvedIssues() {
    return unresolvedIssues;
  }

  public long getFixedIssues() {
    return fixedIssues;
  }

  public long getFpIssues() {
    return fpIssues;
  }

  public long getWfIssues() {
    return wfIssues;
  }

  public List<Issue> getIssues() {
    return issues;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();

    String spacing = "\t";

    sb.append(key)
            .append(spacing).append("LoC: ").append(linesOfCode)
            .append(spacing).append("Cyc: ").append(cyclomaticComplexity)
            .append(spacing).append("Cog: ").append(cognitiveComplexity);
    if (!issues.isEmpty()) {
      sb.append(spacing).append("Un: ").append(unresolvedIssues)
              .append(spacing).append("Fx: ").append(fixedIssues)
              .append(spacing).append("WF: ").append(wfIssues)
              .append(spacing).append("FP: ").append(fpIssues);
    }
    return sb.toString();
  }

  @Override
  public int compareTo(Project o) {
    if (this.getLinesOfCode() == o.getLinesOfCode()) {
      if (this.getCognitiveComplexity() == o.getCognitiveComplexity()) {
        if (this.getCyclomaticComplexity() == o.getCyclomaticComplexity()) {
          return 0;
        }
        return Long.valueOf(this.getCyclomaticComplexity()).compareTo(o.getCyclomaticComplexity());
      }
      return Long.valueOf(this.getCognitiveComplexity()).compareTo(o.getCognitiveComplexity());
    }
    return Long.valueOf(this.getLinesOfCode()).compareTo(o.getLinesOfCode());
  }

  @Override
  public boolean equals(Object o) {
    if (! (o instanceof Project)){
      return false;
    }
    return this.compareTo((Project)o) == 0;
  }


}