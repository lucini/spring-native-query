package io.github.gasparbarancelli;

import java.util.Map;

public interface ProcessorSql {
    void execute(String sql, Map<String, String> replaceSql);
}
