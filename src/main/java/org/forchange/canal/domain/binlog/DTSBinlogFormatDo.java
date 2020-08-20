
package org.forchange.canal.domain.binlog;


import java.util.Map;

@lombok.Data
public class DTSBinlogFormatDo {

    private Data data;
    private String dbName;
    private Map<String, Object> extraTags;
    private String primaryKey;
    private Long recordID;
    private Long recordTimestamp;
    private String recordType;
    private Source source;
    private String tableName;
}
