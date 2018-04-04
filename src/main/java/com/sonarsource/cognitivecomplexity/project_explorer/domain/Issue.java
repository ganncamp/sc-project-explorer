package com.sonarsource.cognitivecomplexity.project_explorer.domain;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Issue {

  public enum Resolution {
    UNRESOLVED,
    FIXED,
    WONTFIX,
    FALSEPOSITIVE;

    public static Resolution fromString(String string) {
      if (string == null) {
        return UNRESOLVED;
      } else if ("FALSE-POSITIVE".equals(string)) {
        return FALSEPOSITIVE;
      }
      return Resolution.valueOf(string);

    }
  }

  String message;
  Resolution resolution = Resolution.UNRESOLVED;
  int threshold;
  int value;
  List<String> comments = new ArrayList<>();

  public Issue(JSONObject jsonIssue) {

    resolution = Resolution.fromString((String) jsonIssue.get("resolution"));
    message = (String) jsonIssue.get("message");

    String tmp = message.replaceAll(".*from ", "");
    tmp = tmp.replaceAll(" to the", "");
    tmp = tmp.replaceAll(" allowed.*","" );
    String[] pieces = tmp.split(" ");

    value = Integer.valueOf(pieces[0]);
    threshold = Integer.valueOf(pieces[1]);

    List<JSONObject> comments = (JSONArray) jsonIssue.get("comments");
    if (comments != null) {
      for (JSONObject comment : comments) {
        this.comments.add((String) comment.get("htmlText"));
      }
    }
  }

  public String getMessage() {
    return message;
  }

  public Resolution getResolution() {
    return resolution;
  }

  public int getThreshold() {
    return threshold;
  }

  public int getValue() {
    return value;
  }

  public List<String> getComments() {
    return comments;
  }

  public String toString(){
    StringBuilder sb = new StringBuilder();
    sb.append(resolution.toString()).append("\t").append(message);
    if (!comments.isEmpty()) {
      for (String comment : comments) {
        sb.append(System.lineSeparator()).append("\t").append(comment);
      }
    }
    return sb.toString();
  }
}