package com.kiddoware.kidsvideoplayer;

import android.net.Uri;
import android.provider.BaseColumns;

public interface KidsVideoPlayerProviderDefinition {

	public static final String PROVIDER_NAME = "com.kiddoware.kidsvideoplayer.kidsvideoplayerprovider";
	public static final String SELECT_ACTION = "com.kiddoware.kidsvideoplayer.aciton.SELECT_VIDEOS";

	public interface Videos extends BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ PROVIDER_NAME + "/video");

		public static final String MEDIA_ID = "media_id";
		public static final String PATH = "path";
		public static final String TITLE = "title";
		public static final String MEDIA_SIZE = "media_size";
		public static final String DURATION = "duration";
		public static final String DATE_MODIFIED = "date_modified";
		public static final String ACTIVE = "active";
		public static final String MEDIA_TYPE = "media_type";
		public static final String THUMBNAIL_URL = "thumbnail_url";
		public static final String CATEGORYID = "categoryId";
		public static final String PLAYLISTID = "playlistId";
	}

	public interface Categories extends BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ PROVIDER_NAME + "/category");

		public static final String USER_CREATED = "user_created";
		public static final String NAME = "name";
	}

}