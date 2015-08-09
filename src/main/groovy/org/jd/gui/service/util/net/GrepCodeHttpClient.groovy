/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.util.net

import groovy.transform.CompileStatic
import org.jd.gui.api.model.Container

import java.security.DigestInputStream
import java.security.MessageDigest

class GrepCodeHttpClient {
    // See: http://grepcode.com/api
    protected static final String GREPCODE_SOURCEFILE_URL_PREFIX = 'http://grepcode.com/file/'
    protected static final String GREPCODE_SOURCEFILE_URL_SUFFIX = '.java?v=source'

    protected static final String GREPCODE_SOURCEARCHIVE_URL_PREFIX = 'http://grepcode.com/snapshot/'
    protected static final String GREPCODE_SOURCEARCHIVE_URL_SUFFIX = '?rel=file&kind=source&n=0'

    // Caches
    protected static final Map<URI, String> SOURCES = Collections.synchronizedMap(new Cache<URI, String>())
    protected static final Map<URI, String> MD5S = Collections.synchronizedMap(new Cache<URI, String>())
    protected static final Map<URI, Boolean> FAILURES = Collections.synchronizedMap(new Cache<URI, Boolean>())

    static String getCachedSourceFile(Container.Entry entry) {
        return SOURCES.get(entry.uri)
    }

    static String downloadRemoteSourceFile(Container.Entry entry) {
        def parent = entry.container.root.parent

        if (!parent.isDirectory() && parent.path.toLowerCase().endsWith('.jar') && !FAILURES.containsKey(parent.uri)) {
            // Search MD5 checksum
            String md5Checksum = searchMd5Checksum(parent)

            // Cut extension
            def path = entry.path
            def truncatedPath = path.substring(0, path.length() - 6 /* .class */)

            // Build URL
            def url = new URL(GREPCODE_SOURCEFILE_URL_PREFIX + md5Checksum + '/' + truncatedPath + GREPCODE_SOURCEFILE_URL_SUFFIX)

            // Send GET HTTP request
            try {
                def connection = url.openConnection()

                connection.connectTimeout = 10000
                connection.useCaches = true

                switch (connection.responseCode) {
                    case 200:
                    String source = connection.inputStream.text

                    // Store source
                    SOURCES.put(entry.uri, source)

                    return source
                    case 404:
                        // Store failure to block future searches on this JAR file
                        FAILURES.put(parent.uri, Boolean.TRUE)
                }
            } catch(Exception ignore) {
            }
        }

        return null
    }

    static InputStream downloadRemoteSourceArchive(Container.Entry entry) {
        if (! entry.isDirectory()) {
            // Search MD5 checksum
            String md5Checksum = searchMd5Checksum(entry)

            // Build URL
            def url = new URL(GREPCODE_SOURCEARCHIVE_URL_PREFIX + md5Checksum + GREPCODE_SOURCEARCHIVE_URL_SUFFIX)

            // Send GET HTTP request
            try {
                def connection = url.openConnection()

                connection.connectTimeout = 20000
                connection.useCaches = true

                if(connection.responseCode == 200) {
                    return connection.inputStream
                }
            } catch(Exception ignore) {
            }
        }

        return null
    }

    @CompileStatic
    protected static String searchMd5Checksum(Container.Entry entry) {
        String md5Checksum = MD5S.get(entry.uri)

        if (md5Checksum == null) {
            // Compute MD5 checksum
            def md = MessageDigest.getInstance('MD5')
            def dis = new DigestInputStream(entry.inputStream, md)
            byte[] buffer = new byte[1024 * 10];

            try {
                while (dis.read(buffer) != -1);
            } finally {
                dis.close()
            }

            byte[] bytes = md.digest()
            def bigInt = new BigInteger(1, bytes)
            md5Checksum = bigInt.toString(16)

            MD5S.put(entry.uri, md5Checksum)
        }

        return md5Checksum
    }

    @CompileStatic
    protected static class Cache<K, V> extends LinkedHashMap<K, V> {
        public static final int CACHE_MAX_ENTRIES = 100

        public Cache() {
            super(CACHE_MAX_ENTRIES*3/2 as int, 0.7f, true)
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > CACHE_MAX_ENTRIES
        }
    }
}
