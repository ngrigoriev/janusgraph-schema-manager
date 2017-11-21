package com.newforma.titan.schema.actions;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;

public class ReindexAction {
	public enum IndexTarget { ALL, NEW, NAMED }

	private final IndexTarget target;
	private final String indexName;;

	public ReindexAction(IndexTarget target) {
		this(target, null);
	}

	public ReindexAction(IndexTarget target, String indexName) {
		Preconditions.checkNotNull(target, "Reindex target cannot be null");
		Preconditions.checkArgument(
				target != IndexTarget.NAMED || (target == IndexTarget.NAMED && !StringUtils.isEmpty(indexName)),
				"Index name must be provided with NAMED target");
		this.target = target;
		this.indexName = indexName;
	}

	public IndexTarget getTarget() {
		return target;
	}

	public String getIndexName() {
		return indexName;
	}
}
