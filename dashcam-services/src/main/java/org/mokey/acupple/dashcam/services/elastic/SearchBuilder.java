package org.mokey.acupple.dashcam.services.elastic;

import org.elasticsearch.index.query.*;
import org.mokey.acupple.dashcam.services.models.LogSearchParam;
import org.mokey.acupple.dashcam.common.utils.Strings;

import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Created by Yuan on 2015/7/2.
 */
public class SearchBuilder {

    private static final String TIME_RANGE = "timestamp";
    private static final String SOURCE_TERM = "source";
    private static final String TITLE_TERM = "title";
    private static final String ENV_GROUP_TERM = "envGroup";
    private static final String HOST_NAME_TERM = "hostName";
    private static final String LEVEL_TERM = "logLevel";
    private static final String LOG_TYPE_TERM = "logType";
    private static final String HOST_IP_TERM = "hostIp";
    private static final String TAGS_TERM = "tags";
    private static final String EXACT_MATCH = "\"";
    private static final String WILDCARD = "*";
    private static final String TRACE_ID = "traceId";

    public FilterBuilder buildFilter(LogSearchParam param) {

        BoolFilterBuilder boolFilterBuilder = FilterBuilders
                .boolFilter()
                .must(FilterBuilders.rangeFilter(TIME_RANGE)
                        .from(param.getFromDate().getTime())
                        .to(param.getToDate().getTime()))
                .must(FilterBuilders.termFilter(ENV_GROUP_TERM, param.getEnvGroup()
                        .toLowerCase()));
        if (param.isTraceOnly()) {
            boolFilterBuilder.must(FilterBuilders.rangeFilter(TRACE_ID).gt(0L));
        }
        if (param.getLogLevels() != null && !param.getLogLevels().isEmpty()) {
            String[] levels = new String[param.getLogLevels().size()];
            for (int i = 0; i < levels.length; i++) {
                levels[i] = param.getLogLevels().get(i).name().toLowerCase();
            }
            boolFilterBuilder.must(FilterBuilders.termsFilter(LEVEL_TERM,
                    levels));
        }
        if (param.getLogTypes() != null && !param.getLogTypes().isEmpty()) {
            String[] types = new String[param.getLogTypes().size()];
            for (int i = 0; i < types.length; i++) {
                types[i] = param.getLogTypes().get(i).name().toLowerCase();
            }
            boolFilterBuilder.must(FilterBuilders.termsFilter(LOG_TYPE_TERM,
                    types));
        }
        if (!Strings.isNullOrEmpty(param.getHostName())) {
            boolFilterBuilder.must(FilterBuilders.queryFilter(QueryBuilders
                    .queryStringQuery(EXACT_MATCH + param.getHostName().toLowerCase() + EXACT_MATCH).field(
                            HOST_NAME_TERM)));
        }
        if (!Strings.isNullOrEmpty(param.getHostIp())) {
            boolFilterBuilder.must(FilterBuilders.termFilter(HOST_IP_TERM,
                    param.getHostIp().toLowerCase()));
        }
        if (param.getTags() != null && !param.getTags().isEmpty()) {
            for (String key : param.getTags().keySet()) {
                String tagKey = param.getTags().get(key);
                if (Strings.isContainChinese(tagKey)) {
                    tagKey = tagKey.replace("*", "").replace("?", "");
                }
                boolFilterBuilder
                        .must(FilterBuilders.queryFilter(QueryBuilders
                                .queryStringQuery(tagKey)
                                .field(key)));
            }
        }
        // Here, title query string use wildcard query, the performance here is
        // very bad
        if (!Strings.isNullOrEmpty(param.getTitle())) {
            String title = param.getTitle().toLowerCase();
            if (!Strings.isContainChinese(title)) {
                title = WILDCARD + title + WILDCARD;
            }
            boolFilterBuilder.must(FilterBuilders.queryFilter(QueryBuilders
                    .queryStringQuery(
                            title).field(TITLE_TERM)));
        }
        if (!Strings.isNullOrEmpty(param.getSource())) {
            boolFilterBuilder.must(FilterBuilders.queryFilter(QueryBuilders
                    .queryStringQuery(
                            WILDCARD + param.getSource().toLowerCase()
                                    + WILDCARD).field(SOURCE_TERM)));
        }
        return boolFilterBuilder;
    }

    public QueryBuilder build(LogSearchParam param) {

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        queryBuilder.must(rangeQuery(TIME_RANGE).from(
                param.getFromDate().getTime()).to(param.getToDate().getTime()));
        queryBuilder.must(termQuery(ENV_GROUP_TERM, param.getEnv()));

        if (!Strings.isNullOrEmpty(param.getHostName())) {
            queryBuilder.must(termQuery(HOST_NAME_TERM, param.getHostName()));
        }
        if (!Strings.isNullOrEmpty(param.getHostIp())) {
            queryBuilder.must(termQuery(HOST_IP_TERM, param.getHostIp()));
        }
        if (param.getTags() != null && !param.getTags().isEmpty()) {
            for (String key : param.getTags().keySet()) {
                queryBuilder.must(termQuery(TAGS_TERM + "."
                        + param.getTags().get(key), param.getTags().get(key)));
            }
        }

        return queryBuilder;
    }
}
