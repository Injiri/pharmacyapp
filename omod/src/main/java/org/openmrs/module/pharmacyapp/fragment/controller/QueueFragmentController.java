package org.openmrs.module.pharmacyapp.fragment.controller;

import org.openmrs.api.context.Context;
import org.openmrs.module.hospitalcore.model.PatientSearch;
import org.openmrs.module.inventory.InventoryService;
import org.openmrs.module.inventory.util.PagingUtil;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by USER on 3/9/2016.
 */
public class QueueFragmentController {
    public void controller(){

    }
    public List<SimpleObject> searchPatient(
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "searchKey", required = false) String searchKey,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "pgSize", required = false) Integer pgSize,
            UiUtils uiUtils) {
        if (pgSize == null) {
            pgSize = Integer.MAX_VALUE;
        }
        InventoryService inventoryService = Context.getService(InventoryService.class);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<PatientSearch> patientSearchList = inventoryService.searchListOfPatient(date, searchKey, currentPage,pgSize);
        if (currentPage == null) currentPage = 1;
        int total = inventoryService.countSearchListOfPatient(date, searchKey, currentPage);
        PagingUtil pagingUtil = new PagingUtil(pgSize, currentPage, total);

        return SimpleObject.fromCollection(patientSearchList,uiUtils,"fullname", "identifier", "age", "gender","patientId");

    }



}
