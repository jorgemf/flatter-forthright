package com.livae.ff.api.util;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class CacheUtil {

	public static void invalidateUserCommentsCache(Long userId) {
		MemcacheService cache = MemcacheServiceFactory.getMemcacheService("comments");
		cache.delete(createUserCommentsCacheKey(userId, null));
	}

	public static String createUserCommentsCacheKey(Long userId, String cursor) {
		return Long.toString(userId) + "_" + (cursor == null ? "" : cursor);
	}

}
