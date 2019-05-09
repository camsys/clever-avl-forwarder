package org.onebusaway.forwarder.service;

import org.onebusaway.forwarder.dao.CleverAvlDao;
import org.onebusaway.forwarder.models.CleverAvlData;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class CleverAvlService {

    @Inject
    CleverAvlDao _cleverAvlDao;

    public List<CleverAvlData> getCleverAvl() throws Exception{
        return _cleverAvlDao.getCleverAvlData();
    }
}
