package com.newforma.titan.schema.actions;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;

public class ReindexAction {
	public enum IndexTarget { ALL, NEW, NAMED, UNAVAILABLE }

	public enum IndexingMethod { LOCAL, HADOOP, HADOOP2 }

	private final IndexTarget target;
	private final String indexName;
	private final IndexingMethod method;

	public ReindexAction(IndexTarget target) {
		this(target, IndexingMethod.LOCAL, null);
	}

	public ReindexAction(IndexTarget target, IndexingMethod method, String indexName) {
		Preconditions.checkNotNull(target, "Reindex target cannot be null");
		Preconditions.checkArgument(
				target != IndexTarget.NAMED || (target == IndexTarget.NAMED && !StringUtils.isEmpty(indexName)),
				"Index name must be provided with NAMED target");
		this.target = target;
		this.method = method;
		this.indexName = indexName;
	}

	public IndexTarget getTarget() {
		return target;
	}

	public String getIndexName() {
		return indexName;
	}

    public IndexingMethod getMethod() {
        return method;
    }
}
