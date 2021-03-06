package solr.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * @author xiaozhi
 * @version 2010-7-20 下午02:57:04
 * @param <T>
 */


/**
 * 普通不带负载均衡检索
 */
//@Service
public class SolrQueryImpl<T> {

    private static Log logger = LogFactory.getLog(SolrQueryImpl.class);

    @Value(value = "#{globalProperties['solr.soTimeOut']}")
    private int soTimeOut;
    @Value(value = "#{globalProperties['solr.connectionTimeOut']}")
    private int connectionTimeOut;
    @Value(value = "#{globalProperties['solr.maxConnectionsPerHost']}")
    private int maxConnectionsPerHost;
    @Value(value = "#{globalProperties['solr.maxTotalConnections']}")
    private int maxTotalConnections;
    @Value(value = "#{globalProperties['solr.maxRetries']}")
    private int maxRetries;


    private HttpSolrServer httpSolrServer;

    private final static String ASC = "asc";



     /**
     * 分页检索
     * @param propertyMap 查询字段
     * @param compositorMap 排序字段
     * @param highlightArray 高亮字段
     * @param classz
     * @param startIndex 开始记录下标
     * @param pageSize 每页条数
     * @param isFacet 是否返回相关查询检索词列表
     * @return
     * @throws Exception
     */
    public SolrResult<T> query(Map<String, String> propertyMap,Map<String, String> compositorMap,
            String[] highlightArray,Class<T> classz,Integer startIndex, Integer pageSize,boolean isFacet)
            throws Exception {
        SolrQuery query = new SolrQuery();
        // 设置搜索字段
        if (null == propertyMap) {
            throw new Exception("搜索字段不可为空!");
        } else {
            for (Object o : propertyMap.keySet()) {
                StringBuffer sb = new StringBuffer();
                sb.append(o.toString()).append(":");
                sb.append(propertyMap.get(o));
                String queryString = addBlank2Expression(sb.toString());
                if(logger.isDebugEnabled())logger.debug("=====================>queryString :"+queryString);
                query.setQuery(queryString);
            }
        }
        //设置是否获得查询分组列表(即相关检索)
        if(isFacet){
               query.setIncludeScore(true);//是否按每组数量高低排序
               query.setFacet(true);//是否分组查询
               //query.setRows(0);//设置返回结果条数，如果你时分组查询，你就设置为0
               query.addFacetField("text");//增加分组字段
               query.setFacetLimit(10);//限制每次返回结果数
               query.setFacetMinCount(1);//限制最少返回1条才能入组

        }

        //设置高亮
        if (null != highlightArray && !"*".equals(propertyMap.get("text"))) {
            query.setHighlight(true);
            for (String field : highlightArray) {
              query.addHighlightField(field);
            }
        }

        // 设置排序条件
        if (null != compositorMap) {
            for (Object co : compositorMap.keySet()) {
                if (ASC == compositorMap.get(co)|| ASC.equals(compositorMap.get(co))) {
                    query.addSortField(co.toString(), SolrQuery.ORDER.asc);
                } else {
                    query.addSortField(co.toString(), SolrQuery.ORDER.desc);
                }
            }
        }

        if (null != startIndex) {
            query.setStart(startIndex);
        }
        if (null != pageSize && 0L != pageSize.longValue()) {
            query.setRows(pageSize);
        }

        try {
            QueryResponse qrsp = httpSolrServer.query(query);
            long hits = qrsp.getResults().getNumFound();//获取结果总数
            if(hits<=0)return new SolrResult<T>(0, null, null,null,0);

            List<T> docs = qrsp.getBeans(classz);
            List<Count> counts= new ArrayList<Count>();
            if(isFacet){
             counts= qrsp.getFacetField("text").getValues();
            }
            int qtime = qrsp.getQTime();

            if (null != highlightArray) {//返回高亮字段
              return new SolrResult<T>(qtime, counts, docs,qrsp.getHighlighting(),hits);
            }else{
              return new SolrResult<T>(qtime, counts, docs,null,hits);
            }
        } catch (Exception e) {
        	logger.error("--------------------------> error :"+e.toString(),e);
            throw new Exception(e);
        }
    }


    private String addBlank2Expression(String oldExpression) {
        String lastExpression;
        lastExpression = oldExpression.replace(" ", " OR ").replace("+", " AND ").replace("-"," NOT ");
        return lastExpression;
    }


    public void setCommonsHttpSolrServer(HttpSolrServer httpSolrServer) {
        this.httpSolrServer = httpSolrServer;
        this.httpSolrServer.setSoTimeout(soTimeOut);
        this.httpSolrServer.setConnectionTimeout(connectionTimeOut);
        this.httpSolrServer.setDefaultMaxConnectionsPerHost(maxConnectionsPerHost);
        this.httpSolrServer.setMaxTotalConnections(maxTotalConnections);
        this.httpSolrServer.setFollowRedirects(false);
        this.httpSolrServer.setAllowCompression(true);
        this.httpSolrServer.setMaxRetries(maxRetries);
    }

}