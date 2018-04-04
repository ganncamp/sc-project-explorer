package com.sonarsource.cognitivecomplexity.project_explorer;

import com.sonarsource.cognitivecomplexity.project_explorer.domain.HasMeasures;
import com.sonarsource.cognitivecomplexity.project_explorer.domain.Issue;
import com.sonarsource.cognitivecomplexity.project_explorer.domain.Project;
import com.sonarsource.cognitivecomplexity.project_explorer.domain.ProjectFile;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Main {

  private static final String[] langArr = {"java", "cs", "cpp", "php", "js", "c", "ts", "swift", "abap"};
  private static final List<String> langs = new ArrayList<>(Arrays.asList(langArr));


  public static void main(String [] args) {

    getProjects();
  }


  private static String getRuleKeys() {
    JSONObject rulesBundle = Fetcher.fetchResultPage(1,"rules/search?q=S3776");
    List<JSONObject> jsonRules = (JSONArray) rulesBundle.get("rules");

    StringBuilder ruleKeys = new StringBuilder();

    for (JSONObject jsonRule : jsonRules) {
      ruleKeys.append(jsonRule.get("key")).append(',');
    }
    return ruleKeys.toString();
  }

  private static List<Project> getProjects(){
    List<Project> projects = new ArrayList<>();

    //SonarTS added Cog. Complex. metric in 2 Jan 18 release
    LocalDate analysisCutoff = LocalDate.parse("2018-01-10");

    // get first half project set
    List<JSONObject> projectBundle = Fetcher.fetchPaginatedResults("components/search_projects?f=analysisDate&s=analysisDate&asc=false" +
            "&filter=ncloc%3E0%20and%20ncloc%3C5000", "components");
    // get second half project set
    projectBundle.addAll(Fetcher.fetchPaginatedResults("components/search_projects?f=analysisDate&s=analysisDate&asc=false" +
            "&filter=ncloc%3E%3D5000", "components"));
    for (JSONObject projectJson : projectBundle) {

      String date = ((String)projectJson.get("analysisDate")).substring(0,10);
      LocalDate analysisDate = LocalDate.parse(date);

      if (analysisDate.isAfter(analysisCutoff)) {
        Project project = new Project((String) projectJson.get("organization"), (String) projectJson.get("key"), (String) projectJson.get("name"));
        try {
          getProjectFiles(project);
        } catch (ExplorerException e) {
        }

        if (project.getCognitiveComplexity() > 0) {
          projects.add(project);
        }
      }
      else {
        break;
      }
    }

    return projects;
  }

  private static void getProjectMeasures(Project project) {
    JSONObject measuresBundle = Fetcher.fetchResultPage(1, "measures/component?" +
            "metricKeys=cognitive_complexity,complexity,ncloc" +
            "&componentKey=" + project.getKey());
    List<JSONObject> measuresList = (JSONArray) ((JSONObject)measuresBundle.get("component")).get("measures");

    saveMeasures(project, measuresList);
  }

  private static void getProjectFiles(Project project) {

    List<JSONObject> pageBundle = Fetcher.fetchPaginatedResults("measures/component_tree?" +
            "qualifiers=FIL" +
            "&metricSortFilter=withMeasuresOnly" +
            "&metricSort=cognitive_complexity" +
            "&metricKeys=cognitive_complexity,complexity,ncloc" +
            "&s=metric" +
            "&component=" + project.getKey()
    , "components");

    Path file = Paths.get("fileMetrics.txt");
    try (BufferedWriter writer = Files.newBufferedWriter(file, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE)) {

      for (JSONObject obj : pageBundle) {
        String language = (String)obj.get("language");
        if (langs.contains(language)) {
          ProjectFile pf = new ProjectFile((String) obj.get("key"), language);
          saveMeasures(pf, (JSONArray) obj.get("measures"));
          if (pf.isValid()) {
            project.addProjectFile(pf);

            writer.write(pf.toString());

            System.out.print(pf);
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private static void saveMeasures(HasMeasures hasMeasures, List<JSONObject> measuresList) {
    for (JSONObject measure : measuresList) {
      long value = Long.parseLong((String)measure.get("value"));
      switch ((String)measure.get("metric")) {
        case "ncloc":
          hasMeasures.setLinesOfCode(value);
          break;
        case "cognitive_complexity":
          hasMeasures.setCognitiveComplexity(value);
          break;
        case "complexity":
          hasMeasures.setCyclomaticComplexity(value);
          break;
      }
    }
  }


  private static void deDup(List<Project> projects) {
    Collections.sort(projects);

    Project previous = null;
    for (Iterator<Project> itr = projects.iterator(); itr.hasNext();) {
      Project proj = itr.next();
      if (proj.equals(previous)) {
        itr.remove();
      } else {
        previous = proj;
      }
    }
  }

  private static void getProjectIssues(Map<String,Project> projectMap) {
    String ruleKeys = getRuleKeys();


    JSONObject issuesBundle = Fetcher.fetchResultPage(1,
            "issues/search?resolutions=FALSE-POSITIVE%2CWONTFIX&ps=1&facets=projectUuids&rules=" + ruleKeys);

    List<JSONObject> uuids = (JSONArray) ((JSONObject)((JSONArray)issuesBundle.get("facets")).get(0)).get("values");
    for (JSONObject uuidObj : uuids) {
      String uuid = (String) uuidObj.get("val");

      List<JSONObject> issues = Fetcher.fetchPaginatedResults("issues/search?rules="
              + ruleKeys + "&projectUuids=" + uuid, "issues");

      String projectkey = (String) issues.get(0).get("project");
      Project project = new Project(projectkey, uuid);
      projectMap.put(projectkey, project);

      getProjectMeasures(project);

      for (JSONObject jsonIssue : issues) {
        project.addIssue(new Issue(jsonIssue));
      }

      System.out.println(project.toString());
    }
  }

}


