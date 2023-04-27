package gov.pto.aps.services.Imp;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;

import gov.pto.aps.ApplicationProperties;

@Service
public class ApsApiServiceImp {

    @Autowired
    private ApplicationProperties properties;

    private final Logger logger = LoggerFactory.getLogger(ApsApiServiceImp.class);

    public String start() {
        List<String> userIdList = new ArrayList<String>();
        try {
            List<String[]> userList = readAllLines(Paths.get(properties.getUserFilePath()));
            String userId;
            for (int i = 1; i < userList.size(); i++) {

                userId = getAlfrescoUserByEmail(userList.get(i)[0]);
                if (userId != null) {
                    userIdList.add(userId);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> groupsIdList = getAlfrescoGroupIds();
        logger.info("User Ids: " + userIdList);
        logger.info("Groups Ids: " + groupsIdList);
        if (userIdList.size() > 0)
            for (int i = 0; i < userIdList.size(); i++)
                for (int j = 0; j < groupsIdList.size(); j++)
                    addAlfrescoUserToGroup(userIdList.get(i), groupsIdList.get(j));

        return "Done";

    }

    public List<String[]> readAllLines(Path filePath) throws Exception {
        try (Reader reader = Files.newBufferedReader(filePath)) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                return csvReader.readAll();
            }
        }
    }

    public void addAlfrescoUserToGroup(String userId, String groupId) {

        String result;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(properties.getServerURL() + properties.getActivitiURL()
                + "enterprise/admin/groups/" + groupId + "/members/"
                + userId);
        httpPost.addHeader("Authorization", "Basic " + getEncryptedBasicAuthorizationCreds());

        try {
            httpPost.setHeader("Accept", "*/*");
            httpPost.setHeader("Content-type", "*/*");
            CloseableHttpResponse response = client.execute(httpPost);

            result = EntityUtils.toString(response.getEntity(), "UTF-8");
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                logger.info("User (" + userId + ") Added to Group (" + groupId + ") Successfully");

            } else {
                logger.error("ERROR in Add User To Group with code (" + statusCode
                        + "): " + result);
            }

        } catch (UnsupportedEncodingException ex) {
            logger.error("ERROR in Add User To Group with code : " + ex.getMessage());
        } catch (IOException ex) {

            logger.error("ERROR in Add User To Group with code : " + ex.getMessage());
        }

    }

    public String getAlfrescoUserByEmail(String email) {

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(
                properties.getServerURL() + properties.getActivitiURL() + "enterprise/users?email=" + email);

        httpGet.addHeader("Authorization", "Basic " + getEncryptedBasicAuthorizationCreds());

        httpGet.setHeader("Accept", "application/json");

        try {

            CloseableHttpResponse response = client.execute(httpGet);

            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                JSONObject tempResult = new JSONObject(result);

                if (!tempResult.isEmpty() && tempResult.has("data")) {
                    if (tempResult.getJSONArray("data").length() > 0) {
                        return String.valueOf(tempResult.getJSONArray("data").getJSONObject(0).getInt("id"));
                    }
                }

            } else {
                logger.error("ERROR in getAlfrescoUserByEmail with code ("
                        + statusCode + "): " + result);
            }

        } catch (UnsupportedEncodingException ex) {
            logger.error("ERROR in getAlfrescoUserByEmail " + ex.getMessage());
        } catch (IOException ex) {
            logger.error("ERROR in getAlfrescoUserByEmail " + ex.getMessage());
        }

        return "";
    }

    public List<String> getAlfrescoGroupIds() {

        List<String> groupIds = new ArrayList<>();
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(properties.getServerURL() + properties.getActivitiURL() + "enterprise/groups");

        httpGet.addHeader("Authorization", "Basic " + getEncryptedBasicAuthorizationCreds());
        httpGet.setHeader("Accept", "application/json");

        try {

            CloseableHttpResponse response = client.execute(httpGet);

            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                JSONObject tempResult = new JSONObject(result), temp;

                if (!tempResult.isEmpty() && tempResult.has("data")) {
                    if (tempResult.getJSONArray("data").length() > 0) {

                        JSONArray array = tempResult.getJSONArray("data");
                        for (Object object : array) {
                            temp = (JSONObject) object;
                            groupIds.add(String.valueOf(temp.getInt("id")));
                        }

                        return groupIds;
                    }
                }

            } else {
                logger.error("ERROR in getAlfrescoGroupIds with code ("
                        + statusCode + "): " + result);
            }

        } catch (UnsupportedEncodingException ex) {
            logger.error("ERROR in getAlfrescoGroupIds " + ex.getMessage());
        } catch (IOException ex) {
            logger.error("ERROR in getAlfrescoGroupIds " + ex.getMessage());
        }

        return groupIds;
    }

    private String getEncryptedBasicAuthorizationCreds() {
        String creds = "";
        creds = properties.getUserName() + ":" + properties.getPassword();
        Base64 base64 = new Base64();
        creds = new String(base64.encode(creds.getBytes()));
        return creds;
    }

}
