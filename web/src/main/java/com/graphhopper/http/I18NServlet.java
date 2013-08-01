/*
 *  Licensed to GraphHopper and Peter Karich under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 * 
 *  GraphHopper licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in 
 *  compliance with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.http;

import com.graphhopper.util.Helper;
import com.graphhopper.util.TranslationMap;
import com.graphhopper.util.TranslationMap.Translation;
import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.*;
import org.json.JSONObject;

/**
 * @author Peter Karich
 */
public class I18NServlet extends GHServlet
{
    @Inject
    private TranslationMap map;

    @Override
    public void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException
    {
        try
        {
            String lang = "";
            String path = req.getPathInfo();
            if (!Helper.isEmpty(path) && path.startsWith("/"))
                lang = path.substring(1);

            if (Helper.isEmpty(lang))
            {
                // fall back to language specified in header e.g. via browser settings
                String acceptLang = req.getHeader("Accept-Language");
                if (!Helper.isEmpty(acceptLang))
                    lang = acceptLang.split(",")[0];
            }

            lang = lang.replaceAll("_", "-");
            if (lang.indexOf("-") > 0)
                lang = lang.substring(0, lang.indexOf("-"));

            Translation tr = map.get(lang);
            JSONObject json = new JSONObject();
            if (tr != null && !"en".equals(tr.getLanguage())) {
                json.put("default", tr.asMap());
                json.put("language", lang);
            } else {
                json.put("language", "en");
            }
            json.put("en", map.get("en").asMap());
            writeJson(req, res, json);
        } catch (Exception ex)
        {
            logger.error("Error while executing request: " + req.getQueryString(), ex);
            writeError(res, SC_INTERNAL_SERVER_ERROR, "Problem occured:" + ex.getMessage());
        }
    }
}