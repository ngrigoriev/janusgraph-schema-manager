package com.newforma.titan.schema.types;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class DoctagListType {
    private Set<String> tags;

    public DoctagListType(Set<String> tags) {
        setTags(tags);
    }

    public void setTags(Set<String> tags) {
        if (tags == null) {
            this.tags = Collections.unmodifiableSet(new LinkedHashSet<String>(0));
        } else {
            this.tags = Collections.unmodifiableSet(new LinkedHashSet<>(tags));
        }
    }

    public Set<String> getTags() {
        return tags;
    }
}
