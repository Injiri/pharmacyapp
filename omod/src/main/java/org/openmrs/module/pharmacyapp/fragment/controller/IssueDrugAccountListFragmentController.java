package org.openmrs.module.pharmacyapp.fragment.controller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openmrs.Role;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hospitalcore.model.*;
import org.openmrs.module.hospitalcore.util.ActionValue;
import org.openmrs.module.inventory.InventoryService;
import org.openmrs.module.inventory.model.InventoryStoreDrugAccount;
import org.openmrs.module.inventory.model.InventoryStoreDrugAccountDetail;
import org.openmrs.module.inventory.util.DateUtils;
import org.openmrs.module.inventory.util.PagingUtil;
import org.openmrs.module.inventory.util.RequestUtil;
import org.openmrs.module.pharmacyapp.StoreSingleton;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;


/**
 * @author Stanslaus Odhiambo
 *         Created on 3/29/2016.
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
    public Integer getReceiptDrugBatchCount(
            @RequestParam(value = "drugId", required = false) Integer drugId,
            @RequestParam(value = "name", required = false) String name,
            UiUtils ui) {
        if (!name.contains(":")){
            return 0;
        }
        String[] formulations = name.split(":", 2);
        InventoryService service = Context.getService(InventoryService.class);
        InventoryDrugFormulation formulation = service.getDrugFormulation(formulations[0], formulations[1]);
        List<SimpleObject> list = listReceiptDrugs(drugId, formulation.getId(), ui);
        return list.size();
    }


    public List<SimpleObject> listReceiptDrugs(Integer drugId,
                                               Integer formulationId,
                                               UiUtils ui){

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

        return SimpleObject.fromCollection(listReceiptDrugReturn, ui, "id", "drug.id", "formulation.id", "formulation.name", "formulation.dozage", "drug.name", "drug.category.id", "drug.category.name", "dateExpiry", "dateManufacture",
                "companyName", "companyNameShort", "batchNo", "currentQuantity", "costToPatient");
    }

    public List<SimpleObject> listReceiptDrug(
            @RequestParam(value = "drugId", required = false) Integer drugId, UiUtils uiUtils,
            @RequestParam(value = "formulationId", required = false) Integer formulationId) {

        return listReceiptDrugs(drugId, formulationId, uiUtils);
    }
    public InventoryStoreDrugAccount postAccountName(String account) {
        InventoryService inventoryService = Context.getService(InventoryService.class);
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
        InventoryStoreDrugAccount issueAccount = new InventoryStoreDrugAccount();
        issueAccount.setCreatedBy(Context.getAuthenticatedUser().getGivenName());
        issueAccount.setCreatedOn(new Date());
        issueAccount.setName(account);
        issueAccount.setStore(store);

        return issueAccount;
    }


    public String processIssueDrugAccount(HttpServletRequest request, UiUtils uiUtils) {
        String account = request.getParameter("accountName");
        InventoryStoreDrugAccount issueDrugAccount = postAccountName(account);

        InventoryService inventoryService = Context.getService(InventoryService.class);
        int userId = Context.getAuthenticatedUser().getId();
//        String fowardParam = "issueDrugAccountDetail_" + userId;
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

        String selectedDrugs = request.getParameter("selectedDrugs");
        JSONArray selectedDrugsJson = new JSONArray(selectedDrugs);

//        Form the InventoryStoreDrugAccountDetail  from the JSONArray received at the Server level
        //loop over the incoming items

        List<InventoryStoreDrugAccountDetail> list = new ArrayList<InventoryStoreDrugAccountDetail>();
        for (int i = 0; i < selectedDrugsJson.length(); i++) {
            JSONObject incomingItem = selectedDrugsJson.getJSONObject(i);
            JSONObject transactionD = incomingItem.getJSONObject("item");
            String qnty = incomingItem.getString("quantity");
            Integer temp = NumberUtils.toInt(qnty);

            InventoryStoreDrugTransactionDetail transactionDetail = inventoryService.getStoreDrugTransactionDetailById(transactionD.getInt("id"));
            InventoryStoreDrugAccountDetail issueDrugDetail = new InventoryStoreDrugAccountDetail();
            issueDrugDetail.setTransactionDetail(transactionDetail);
            issueDrugDetail.setQuantity(temp);
            list.add(issueDrugDetail);
        }
//
        if (issueDrugAccount != null && list != null && list.size() > 0) {
            Date date = new Date();
            // create transaction issue from substore
            InventoryStoreDrugTransaction transaction = new InventoryStoreDrugTransaction();
            transaction.setDescription("ISSUE DRUG TO ACCOUNT " + DateUtils.getDDMMYYYY());
            transaction.setStore(store);
            transaction.setTypeTransaction(ActionValue.TRANSACTION[1]);
            transaction.setCreatedOn(date);
            transaction.setCreatedBy(Context.getAuthenticatedUser().getGivenName());
            transaction = inventoryService.saveStoreDrugTransaction(transaction);

            issueDrugAccount = inventoryService.saveStoreDrugAccount(issueDrugAccount);
            for (InventoryStoreDrugAccountDetail pDetail : list) {
                Date date1 = new Date();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Integer totalQuantity = inventoryService.sumCurrentQuantityDrugOfStore(store.getId(), pDetail
                                .getTransactionDetail().getDrug().getId(),
                        pDetail.getTransactionDetail().getFormulation().getId());
                int t = totalQuantity - pDetail.getQuantity();

                InventoryStoreDrugTransactionDetail drugTransactionDetail = inventoryService
                        .getStoreDrugTransactionDetailById(pDetail
                                .getTransactionDetail().getId());
                pDetail.getTransactionDetail().setCurrentQuantity(drugTransactionDetail.getCurrentQuantity()
                        - pDetail.getQuantity());
                inventoryService.saveStoreDrugTransactionDetail(pDetail.getTransactionDetail());

                // save transactiondetail first
                InventoryStoreDrugTransactionDetail transDetail = new InventoryStoreDrugTransactionDetail();
                transDetail.setTransaction(transaction);
                transDetail.setCurrentQuantity(0);
                transDetail.setIssueQuantity(pDetail.getQuantity());
                transDetail.setOpeningBalance(totalQuantity);
                transDetail.setClosingBalance(t);
                transDetail.setQuantity(0);
                transDetail.setVAT(pDetail.getTransactionDetail().getVAT());
                transDetail.setCostToPatient(pDetail.getTransactionDetail().getCostToPatient());
                transDetail.setUnitPrice(pDetail.getTransactionDetail()
                        .getUnitPrice());
                transDetail.setDrug(pDetail.getTransactionDetail().getDrug());
                transDetail.setFormulation(pDetail.getTransactionDetail()
                        .getFormulation());
                transDetail.setBatchNo(pDetail.getTransactionDetail()
                        .getBatchNo());
                transDetail.setCompanyName(pDetail.getTransactionDetail()
                        .getCompanyName());
                transDetail.setDateManufacture(pDetail.getTransactionDetail()
                        .getDateManufacture());
                transDetail.setDateExpiry(pDetail.getTransactionDetail()
                        .getDateExpiry());
                transDetail.setReceiptDate(pDetail.getTransactionDetail()
                        .getReceiptDate());
                transDetail.setCreatedOn(date1);
                transDetail.setReorderPoint(pDetail.getTransactionDetail().getDrug().getReorderQty());
                transDetail.setAttribute(pDetail.getTransactionDetail().getDrug().getAttributeName());
                transDetail.setPatientType(pDetail.getTransactionDetail().getPatientType());

                BigDecimal moneyUnitPrice = pDetail.getTransactionDetail().getCostToPatient()
                        .multiply(new BigDecimal(pDetail.getQuantity()));
                transDetail.setTotalPrice(moneyUnitPrice);
                transDetail.setParent(pDetail.getTransactionDetail());
                transDetail = inventoryService.saveStoreDrugTransactionDetail(transDetail);

                pDetail.setDrugAccount(issueDrugAccount);
                pDetail.setTransactionDetail(transDetail);
                // save issue to patient detail
                inventoryService.saveStoreDrugAccountDetail(pDetail);
                // save issues transaction detail
            }
        }

        return "success";
    }
}
