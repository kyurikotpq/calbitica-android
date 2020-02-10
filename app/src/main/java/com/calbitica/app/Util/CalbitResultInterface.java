package com.calbitica.app.Util;

import com.calbitica.app.Models.Calbit.Calbit;

import java.util.List;

// For handling the result of calbit-related http calls
// CRUD + completion
public interface CalbitResultInterface {
    void onCalbitListResult(List<Calbit> calbitList);
    void onCalbitCompletionFailure();
}
