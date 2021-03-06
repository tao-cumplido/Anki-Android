/***************************************************************************************
 * Copyright (c) 2012 Norbert Nagold <norbert.nagold@gmail.com>                         *
 * Copyright (c) 2014 Timothy Rae <perceptualchaos2@gmail.com>                          *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/

package com.ichi2.libanki.sync;

import com.ichi2.anki.AnkiDroidApp;
import com.ichi2.anki.exception.UnknownHttpResponseException;
import com.ichi2.async.Connection;
import com.ichi2.libanki.Consts;
import com.ichi2.libanki.Utils;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;

public class RemoteServer extends HttpSyncer {

    public RemoteServer(Connection con, String hkey) {
        super(hkey, con);
    }


    /** Returns hkey or none if user/pw incorrect. 
     * @throws UnknownHttpResponseException */
    @Override
    public HttpResponse hostKey(String user, String pw) throws UnknownHttpResponseException {
        try {
            mPostVars = new HashMap<String, Object>();
            JSONObject jo = new JSONObject();
            jo.put("u", user);
            jo.put("p", pw);
            return super.req("hostKey", super.getInputStream(Utils.jsonToString(jo)));
        } catch (JSONException e) {
            return null;
        }
    }


    @Override
    public HttpResponse register(String user, String pw) throws UnknownHttpResponseException {
        try {
            JSONObject jo = new JSONObject();
            jo.put("u", URLEncoder.encode(user, "UTF-8"));
            jo.put("p", URLEncoder.encode(pw, "UTF-8"));
            return super.req("register", null, 6, jo);
        } catch (JSONException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }


    @Override
    public HttpResponse meta() throws UnknownHttpResponseException {
        try {
            mPostVars = new HashMap<String, Object>();
            mPostVars.put("k", mHKey);
            mPostVars.put("s", mSKey);
            JSONObject jo = new JSONObject();
            jo.put("v", Consts.SYNC_VER);
            jo.put("cv",
                    String.format(Locale.US, "ankidroid,%s,%s", AnkiDroidApp.getPkgVersionName(), Utils.platDesc()));
            return super.req("meta", super.getInputStream(Utils.jsonToString(jo)));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public JSONObject applyChanges(JSONObject kw) throws UnknownHttpResponseException {
        return _run("applyChanges", kw);
    }


    @Override
    public JSONObject start(JSONObject kw) throws UnknownHttpResponseException {
        return _run("start", kw);
    }


    @Override
    public JSONObject chunk() throws UnknownHttpResponseException {
        JSONObject co = new JSONObject();
        return _run("chunk", co);
    }


    @Override
    public void applyChunk(JSONObject sech) throws UnknownHttpResponseException {
        _run("applyChunk", sech);
    }


    @Override
    public JSONObject sanityCheck2(JSONObject client) throws UnknownHttpResponseException {
        return _run("sanityCheck2", client);
    }


    @Override
    public long finish() throws UnknownHttpResponseException {
        try {
            HttpResponse ret = super.req("finish", super.getInputStream("{}"));
            String s = super.stream2String(ret.getEntity().getContent());
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0;
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private JSONObject _run(String cmd, JSONObject data) throws UnknownHttpResponseException {
        HttpResponse ret = super.req(cmd, super.getInputStream(Utils.jsonToString(data)));
        try {
            String s = super.stream2String(ret.getEntity().getContent());
            if (!s.equalsIgnoreCase("null") && s.length() != 0) {
                return new JSONObject(s);
            } else {
                return new JSONObject();
            }
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
