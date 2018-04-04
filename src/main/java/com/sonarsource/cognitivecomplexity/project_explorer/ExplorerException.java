package com.sonarsource.cognitivecomplexity.project_explorer;/*
 * Copyright (C) 2014-2018 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */

public class ExplorerException extends RuntimeException {

  public ExplorerException(String message) {
    super(message);
  }

  public ExplorerException(Exception e) {
    super(e);
  }
}
