package org.jz.controller;

import com.alibaba.fastjson.JSONObject;
import org.jz.common.constant.SteamConstants;
import org.jz.ext.steam.ApiListService;
import org.jz.ext.steam.AppListService;
import org.jz.model.steam.SteamApi;
import org.jz.model.steam.SteamApp;
import org.jz.service.SteamApiService;
import org.jz.service.SteamAppService;
import org.jz.util.CacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 *
 * Get steam web api access:
 * Wiki:Most WebAPI methods take the following arguments in their URL:
 *      1.key:Your Steam Web API key. Without this, the server will return an HTTP 403 (forbidden) error.
 *      2.format(Optional):The file format to return output in.
 *          json (default):The JavaScript Object Notation format
 *          xml:Standard XML
 *      3.vdf:Valve Data Format
 * @author Hongyi Zheng
 * @date 2018/2/8
 */

@RestController
@RequestMapping("/steam")
public class SteamApiController {

    private final static Logger logger = LoggerFactory.getLogger(SteamApiController.class);

    @Autowired
    ApiListService apiListService;

    @Autowired
    SteamApiService steamApiService;

    @Autowired
    SteamAppService steamAppService;

    @Autowired
    AppListService appListService;

    /**
     * wrap ${@link org.jz.ext.steam.ApiEnum} APIs
     * @return
     */
    @RequestMapping("apilist")
    public JSONObject getApiListService() throws IOException {

        List<SteamApi> list = steamApiService.queryAll();
        JSONObject rspJson;
        //TODO considering to write the list to a file and backup to local hard-disk
        //search db if expired
        if (null!=list &&list.size()>0) {
            if (CacheUtils.isExpired(SteamConstants.API_LIST_CACHE,list.get(0).getOutime())) {
                steamApiService.delAll();
                //call external Api
                rspJson = apiListService.callApiList();
                list = apiListService.parseToModel(rspJson);
                for (SteamApi steamApi : list) {
                    try {
                        steamApiService.insertSelective(steamApi);
                    } catch (Exception e) {
                        logger.error("Data insert exception:{}",e.getMessage());
                    }
                }
            }
            return apiListService.parseToJson(list);
        }else {
            //no cache,call external api
            rspJson = apiListService.callApiList();
            //cache to db
            list = apiListService.parseToModel(rspJson);
            for (SteamApi steamApi : list) {
                try {
                    steamApiService.insertSelective(steamApi);
                } catch (Exception e) {
                    logger.error("Data insert exception:{}",e.getMessage());
                }
            }
            return rspJson;
        }

    }

    /**
     *
     * @return
     */
    @RequestMapping(value = "/applist")
    public JSONObject getAppListService() throws IOException {

        List<SteamApp> list = steamAppService.queryAll();
        JSONObject rspJson;

        if (null!=list &&list.size()>0) {
            if (CacheUtils.isExpired(SteamConstants.APP_LIST_CACHE,list.get(0).getOutime())) {
                steamAppService.delAll();

                //call external api
                rspJson = appListService.callAppListApi();
                list = appListService.parseToModel(rspJson);
                for (SteamApp app : list) {
                    try {
                        steamAppService.insertSelective(app);
                    } catch (Exception e) {
                        logger.error("Data insert exception:{}",e.getMessage());
                    }
                }
            }
            //缓存未过期,则直接返回结果
            return appListService.parseToJson(list);
        }else {
            //no cache,call external api
            rspJson = appListService.callAppListApi();
            //cache to db
            list = appListService.parseToModel(rspJson);
            for (SteamApp steamApp : list) {
                try {
                    steamAppService.insertSelective(steamApp);
                } catch (Exception e) {
                    logger.error("Data insert exception:{}",e.getMessage());
                }
            }
            return rspJson;
        }
    }
}
