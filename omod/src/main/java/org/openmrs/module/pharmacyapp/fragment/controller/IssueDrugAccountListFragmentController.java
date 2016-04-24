package org.openmrs.module.pharmacyapp.fragment.controller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Role;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hospitalcore.model.*;
import org.openmrs.module.inventory.InventoryService;
import org.openmrs.module.inventory.model.InventoryStoreDrugAccount;
import org.openmrs.module.inventory.model.InventoryStoreDrugAccountDetail;
import org.openmrs.module.inventory.util.PagingUtil;
import org.openmrs.module.inventory.util.RequestUtil;
import org.openmrs.module.pharmacyapp.StoreSingleton;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Stanslaus Odhiambo
 * Created on 3/29/2016.
 */
public class IssueDrugAccountListFragmentController {
    public void controller() {

    }

    public List<SimpleObject> fetchList(
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "issueName", required = false) String issueName,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            UiUtils uiUtils, HttpServletRequest request) {
        InventoryService inventoryService = (InventoryService) Context.getService(InventoryService.class);

        List<Role> role = new ArrayList<Role>(Context.getAuthenticatedUser().getAllRoles());

        InventoryStoreRoleRelation storeRoleRelation = null;
        Role roleUser = null;
        for (Role rolePerson : role) {
            if (inventoryService.getStoreRoleByName(rolePerson.toString()) != null) {
                storeRoleRelation = inventoryService.getStoreRoleByName(rolePerson.toString());
                roleUser = rolePerson;
            }
        }
        InventoryStore store = null;
        if (storeRoleRelation != null) {
            store = inventoryService.getStoreById(storeRoleRelation.getStoreid());

        }
        int total = inventoryService.countStoreDrugAccount(store.getId(), issueName, fromDate, toDate);
        String temp = "";

        if (issueName != null) {
            if (StringUtils.isBlank(temp)) {
                temp = "?issueName=" + issueName;
            } else {
                temp += "&issueName=" + issueName;
            }
        }
        if (!StringUtils.isBlank(fromDate)) {
            if (StringUtils.isBlank(temp)) {
                temp = "?fromDate=" + fromDate;
            } else {
                temp += "&fromDate=" + fromDate;
            }
        }
        if (!StringUtils.isBlank(toDate)) {
            if (StringUtils.isBlank(temp)) {
                temp = "?toDate=" + toDate;
            } else {
                temp += "&toDate=" + toDate;
            }
        }

        PagingUtil pagingUtil = new PagingUtil(RequestUtil.getCurrentLink(request) + temp, pageSize, currentPage, total);
        List<InventoryStoreDrugAccount> listIssue = inventoryService.listStoreDrugAccount(store.getId(), issueName, fromDate, toDate, pagingUtil.getStartPos(), pagingUtil.getPageSize());

        return SimpleObject.fromCollection(listIssue, uiUtils, "id", "name", "createdOn");
    }


    public List<SimpleObject> listReceiptDrug(
            @RequestParam(value = "drugId", required = false) Integer drugId, UiUtils uiUtils,
            @RequestParam(value = "formulationId", required = false) Integer formulationId) {

        List<InventoryStoreDrugTransactionDetail> listReceiptDrugReturn = new ArrayList<InventoryStoreDrugTransactionDetail>();
        InventoryService inventoryService = Context.getService(InventoryService.class);
        ConceptService conceptService = Context.getConceptService();
        InventoryDrug drug = inventoryService.getDrugById(drugId);
        //InventoryStore store = inventoryService.getStoreByCollectionRole(new ArrayList<Role>(Context.getAuthenticatedUser().getAllRoles()));
        List<Role> role = new ArrayList<Role>(Context.getAuthenticatedUser().getAllRoles());

        InventoryStoreRoleRelation srl = null;
        Role rl = null;
        for (Role r : role) {
            if (inventoryService.getStoreRoleByName(r.toString()) != null) {
                srl = inventoryService.getStoreRoleByName(r.toString());
                rl = r;
            }
        }
        InventoryStore store = null;
        if (srl != null) {
            store = inventoryService.getStoreById(srl.getStoreid());

        }
        if (store != null && drug != null && formulationId != null) {

            List<InventoryStoreDrugTransactionDetail> listReceiptDrug = inventoryService
                    .listStoreDrugTransactionDetail(store.getId(),
                            drug.getId(), formulationId, true);
            // check that drug is issued before
            int userId = Context.getAuthenticatedUser().getId();

            String fowardParam = "issueDrugAccountDetail_" + userId;
            String fowardParamDrug = "issueDrugDetail_" + userId;
            List<InventoryStoreDrugPatientDetail> listDrug = (List<InventoryStoreDrugPatientDetail>) StoreSingleton
                    .getInstance().getHash().get(fowardParamDrug);
            List<InventoryStoreDrugAccountDetail> listDrugAccount = (List<InventoryStoreDrugAccountDetail>) StoreSingleton
                    .getInstance().getHash().get(fowardParam);

            boolean check = false;
            if (CollectionUtils.isNotEmpty(listDrug)) {
                if (CollectionUtils.isNotEmpty(listReceiptDrug)) {
                    for (InventoryStoreDrugTransactionDetail drugDetail : listReceiptDrug) {
                        for (InventoryStoreDrugPatientDetail drugPatient : listDrug) {

                            if (drugDetail.getId().equals(
                                    drugPatient.getTransactionDetail().getId())) {
                                drugDetail.setCurrentQuantity(drugDetail
                                                .getCurrentQuantity()
                                );

                            }

                        }
                        if (drugDetail.getCurrentQuantity() > 0) {

                            listReceiptDrugReturn.add(drugDetail);
                            check = true;
                        }
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(listDrugAccount)) {
                if (CollectionUtils.isNotEmpty(listReceiptDrug)) {
                    for (InventoryStoreDrugTransactionDetail drugDetail : listReceiptDrug) {
                        for (InventoryStoreDrugAccountDetail drugAccount : listDrugAccount) {
                            if (drugDetail.getId().equals(
                                    drugAccount.getTransactionDetail().getId())) {
                                drugDetail.setCurrentQuantity(drugDetail
                                                .getCurrentQuantity()
                                );

                            }
                        }
                        if (drugDetail.getCurrentQuantity() > 0 && !check) {
                            listReceiptDrugReturn.add(drugDetail);
                        }
                    }
                }
            }
            if (CollectionUtils.isEmpty(listReceiptDrugReturn)
                    && CollectionUtils.isNotEmpty(listReceiptDrug)) {
                listReceiptDrugReturn.addAll(listReceiptDrug);
            }


        }

        return SimpleObject.fromCollection(listReceiptDrugReturn, uiUtils, "id", "drug.id","formulation.name","formulation.dozage","drug.name","drug.category.id","drug.category.name","dateExpiry","dateManufacture",
                "companyName","companyNameShort","batchNo","currentQuantity");
    }
}
