package com.alexbt.mock.web;

import com.alexbt.mock.model.MockConfig;
import com.alexbt.mock.model.MockResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author alexbt
 */
@RestController
public class MockController {

    private static final Logger LOG = LoggerFactory.getLogger(MockController.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final MockConfig urlRewriteConfig;

    @Autowired
    public MockController(MockConfig urlRewriteConfig) {
        Validate.notNull(urlRewriteConfig);
        Validate.notNull(urlRewriteConfig.getUrlRewrites());
        Validate.notEmpty(urlRewriteConfig.getResponseLocation());
        this.urlRewriteConfig = urlRewriteConfig;
    }

    @PostMapping(value = "/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> post(HttpServletRequest request, @RequestBody String content) throws IOException {
        return returnResponse(request, content);
    }

    @PutMapping(value = "/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> put(HttpServletRequest request, @RequestBody String content) throws IOException {
        return returnResponse(request, content);
    }

    @PatchMapping(value = "/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> patch(HttpServletRequest request, @RequestBody String content) throws IOException {
        return returnResponse(request, content);
    }

    @GetMapping(value = "/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> get(HttpServletRequest request) throws IOException {
        return returnResponse(request, null);
    }

    @DeleteMapping(value = "/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> delete(HttpServletRequest request) throws IOException {
        return returnResponse(request, null);
    }

    private ResponseEntity<String> returnResponse(HttpServletRequest request, @RequestBody String content) throws IOException {
        String testName = getTestName(content);
        MockResponse mockResponse = getMockResponse(request, testName);

        int status = mockResponse.getStatus();
        return mockResponse.getContent() == null ?
                ResponseEntity.status(status).build() :
                ResponseEntity.status(status).body(mockResponse.getContent().toString());
    }

    private MockResponse getMockResponse(HttpServletRequest request, String testName) throws IOException {
        String uri = StringUtils.removeStart(request.getRequestURI(), "/");
        uri = rewriteUrl(uri);
        File file = resolveResponseFile(uri, request, testName);

        LOG.info("Returning {}", file.getAbsolutePath());
        return OBJECT_MAPPER.readValue(FileUtils.readFileToString(file, Charset.forName("UTF-8")), MockResponse.class);
    }

    private File resolveResponseFile(String uri, HttpServletRequest request, String testName) {
        Assert.notNull(uri, "Must not be empty");
        Assert.notNull(request, "Must not be null");
        Assert.notNull(testName, "Must not be null");

        String httpMethod = request.getMethod().toLowerCase();
        File filenameDefault = new File(urlRewriteConfig.getResponseLocation() + "/" + uri + "/" + "_" + httpMethod + "_response.json");
        File filenameForTestCase = new File(urlRewriteConfig.getResponseLocation() + "/" + uri + "/" + testName + httpMethod + "_response.json");

        File file = filenameForTestCase;
        if (!filenameForTestCase.exists()) {
            file = filenameDefault;
            if (!filenameDefault.exists()) {
                throw new IllegalArgumentException("Can't find response for uri " + uri);
            }
        }
        return file;
    }

    private String rewriteUrl(String incomingUri) {
        for (String key : urlRewriteConfig.getUrlRewrites().keySet()) {
            if (incomingUri.matches(key)) {
                String newUri = urlRewriteConfig.getUrlRewrites().get(key);
                LOG.info("Uri matched '{}', rtewriting uri from '{}' to '{}'", key, incomingUri, newUri);
                return newUri;
            }
        }
        return incomingUri;
    }

    private String getTestName(String content) {
        String testName = "_";
        if (content != null) {
            Pattern p = Pattern.compile(".*?\\[test\\:(?<test>.*?)\\].*", Pattern.DOTALL);
            Matcher matcher = p.matcher(content);
            if (matcher.matches()) {
                testName = matcher.group("test") + "_";
            }
        }
        return testName;
    }
}
