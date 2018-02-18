package com.tools.hot.git.infrastructure;

public class RepoNotFoundException extends RuntimeException {

  public RepoNotFoundException(Throwable cause) {
    super(cause);
  }
}
