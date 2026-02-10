package com.yurikh.yourant.cache;

import com.yurikh.yourant.model.Rant;
import com.yurikh.yourant.network.API;

import java.time.Duration;

public class RantsCache extends Cache<Long, Rant> {

    public RantsCache() {
        super(Duration.ofMinutes(10));
    }

    @Override
    protected Rant load(Long id) throws Exception {
        return API.getRant(id);
    }
}
