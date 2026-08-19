package com.matheus.srv_portfolio_scheduler.domain.services.rebalance;

import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CustodyTickerMigrationOperation {

    public void execute(
            Map<String, Custody> custodies,
            Set<String> removedTickers,
            Set<String> addedTickers) {

        List<String> removedTickerList = new ArrayList<>(removedTickers);
        List<String> addedTickerList = new ArrayList<>(addedTickers);

        for (int i = 0; i < removedTickers.size(); i++) {

            Custody custody = custodies.get(removedTickerList.get(i));
            if (custody == null) continue;

            custody.migrateAsset(addedTickerList.get(i));
        }
    }
}
