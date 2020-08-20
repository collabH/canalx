
package org.forchange.canal.domain.binlog;

import java.util.Map;

@lombok.Data
public class Data {
    private Map<String, String> after;
    private Map<String, String> before;
}
