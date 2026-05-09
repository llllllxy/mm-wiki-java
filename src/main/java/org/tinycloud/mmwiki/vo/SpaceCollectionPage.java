package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.Space;

/**
 * SpaceCollectionPage view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class SpaceCollectionPage {

    /**
     * spaces.
     */
    private List<Space> spaces;

    /**
     * count.
     */
    private int count;

    public SpaceCollectionPage() {
    }

    public SpaceCollectionPage(
            List<Space> spaces,
            int count
    ) {
        this.spaces = spaces;
        this.count = count;
    }

    public List<Space> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<Space> spaces) {
        this.spaces = spaces;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
