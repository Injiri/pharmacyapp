package org.openmrs.module.pharmacyapp.fragment.controller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.cfg.NotYetImplementedException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.Role;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.hospitalcore.InventoryCommonService;
import org.openmrs.module.hospitalcore.model.*;
import org.openmrs.module.hospitalcore.util.ActionValue;
import org.openmrs.module.inventory.InventoryService;
import org.openmrs.module.inventory.model.InventoryStoreDrugIndentDetail;
import org.openmrs.module.inventory.util.DateUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class IssuePatientDrugFragmentController {
    public void controller() {

    }

    public String saveIndentSlip(HttpServletRequest request) {
        String drugOrderString = request.getParameter("drugOrder");
        String indentString = request.getParameter("indentName");
        List<String> errors = new ArrayList<String>();
        InventoryDrug drug = null;
        int drugIdMain = -1;

        JSONArray indentArray = new JSONArray(indentString);
        JSONObject indentObject = indentArray.getJSONObject(0);
        String indentName = indentObject.getString("indentName");
        int mainStoreId = Integer.parseInt(indentObject.getString("mainstore"));

        InventoryService inventoryService = (InventoryService) Context.getService(InventoryService.class);
        Date date = new Date();
        int userId = Context.getAuthenticatedUser().getId();
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
        InventoryStore mainStore = inventoryService.getStoreById(mainStoreId);
        InventoryStoreDrugIndent indent = new InventoryStoreDrugIndent();
        indent.setName(indentName);
        indent.setCreatedOn(date);
        indent.setStore(store);
        indent.setMainStore(mainStore);

        if (!StringUtils.isBlank(request.getParameter("send"))) {
            indent.setMainStoreStatus(1);
            indent.setSubStoreStatus(2);
        } else {
            indent.setMainStoreStatus(0);
            indent.setSubStoreStatus(1);
        }
//        String fowardParam = "subStoreIndentDrug_"+userId;
//        List<InventoryStoreDrugIndentDetail> list = (List<InventoryStoreDrugIndentDetail> ) StoreSingleton.getInstance().getHash().get(fowardParam);
//        if(list != null && list.size() > 0){
//            indent = inventoryService.saveStoreDrugIndent(indent);
//            for(int i=0;i< list.size();i++){
//                InventoryStoreDrugIndentDetail indentDetail = list.get(i);
//                indentDetail.setCreatedOn(date);
//                indentDetail.setIndent(indent);
//                inventoryService.saveStoreDrugIndentDetail(indentDetail);
//            }
//            StoreSingleton.getInstance().getHash().remove(fowardParam);
//            return "success";
//        }else{
//            return "Sorry don't have any indents to save";
//        }

        JSONArray drugArray = new JSONArray(drugOrderString);
        List<InventoryStoreDrugIndentDetail> list = new ArrayList<InventoryStoreDrugIndentDetail>();
//        loop over the incoming items
        for (int i = 0; i < drugArray.length(); i++) {

            JSONObject incomingItem = drugArray.getJSONObject(i);
            System.out.println(incomingItem);
            String drugCategoryId = incomingItem.getString("drugCategoryId");
            String quantity = incomingItem.getString("quantity");
            String drugId = incomingItem.getString("drugId");
            String drugFormulationId = incomingItem.getString("drugFormulationId");
            if (StringUtils.isNotBlank(drugId)) {
                drug = inventoryService.getDrugById(Integer.parseInt(drugId));
            }
            if (drug == null) {
                errors.add("Drug is Required!");

            } else {
                drugIdMain = drug.getId();
            }
            int formulation = Integer.parseInt(drugFormulationId);

            InventoryDrugFormulation formulationO = inventoryService.getDrugFormulationById(formulation);
            if (formulationO == null) {
                errors.add("Formulation is Required.");
            }
            if (formulationO != null && drug != null && !drug.getFormulations().contains(formulationO)) {
                errors.add("Formulation is not correct.");
            }

            if (CollectionUtils.isNotEmpty(errors)) {
                return "error";
            }


            InventoryStoreDrugIndentDetail indentDetail = new InventoryStoreDrugIndentDetail();
            indentDetail.setDrug(drug);
            indentDetail.setFormulation(inventoryService.getDrugFormulationById(formulation));
            indentDetail.setQuantity(Integer.parseInt(quantity));
            list.add(indentDetail);

        }

        if (list != null && list.size() > 0) {
            indent = inventoryService.saveStoreDrugIndent(indent);
            for (int i = 0; i < list.size(); i++) {
                InventoryStoreDrugIndentDetail indentDetail = list.get(i);
                indentDetail.setCreatedOn(date);
                indentDetail.setIndent(indent);
                inventoryService.saveStoreDrugIndentDetail(indentDetail);
            }
        }
        return "success";
    }

    public String processIssueDrug(HttpServletRequest request) {
        String patientType = request.getParameter("patientType");
        String selectedDrugs = request.getParameter("selectedDrugs");
        Integer patientId = Integer.parseInt(request.getParameter("patientId"));
        JSONArray jsonArray = new JSONArray(selectedDrugs);
        List<InventoryStoreDrugPatientDetail> list = new ArrayList<InventoryStoreDrugPatientDetail>();

        InventoryService inventoryService = Context.getService(InventoryService.class);
        int userId = Context.getAuthenticatedUser().getId();
        List<Role> role = new ArrayList<Role>(Context.getAuthenticatedUser().getAllRoles());

        InventoryStoreRoleRelation srl = null;
        Role rl = null;
        for (Role r : role) {
            if (inventoryService.getStoreRoleByName(r.toString()) != null) {
                srl = inventoryService.getStoreRoleByName(r.toString());
                rl = r;
            }
        }
        InventoryStore subStore = null;
        if (srl != null) {
            subStore = inventoryService.getStoreById(srl.getStoreid());

        }
        InventoryStoreDrugPatient issueDrugPatient = new InventoryStoreDrugPatient();
        if (patientId != null && patientId > 0) {
            Patient patient = Context.getPatientService().getPatient(patientId);
            if (patient != null) {

                issueDrugPatient.setCreatedBy(Context.getAuthenticatedUser().getGivenName());
                issueDrugPatient.setCreatedOn(new Date());
                issueDrugPatient.setStore(subStore);
                issueDrugPatient.setIdentifier(patient.getPatientIdentifier().getIdentifier());
                if (patient.getMiddleName() != null) {
                    issueDrugPatient.setName(patient.getGivenName() + " " + patient.getFamilyName() + " " + patient.getMiddleName().replace(",", " "));
                } else {
                    issueDrugPatient.setName(patient.getGivenName() + " " + patient.getFamilyName());
                }
                issueDrugPatient.setPatient(patient);
            }

        }
        //process the incoming json string
        ConceptService conceptService = Context.getConceptService();
        InventoryCommonService inventoryCommonService = Context.getService(InventoryCommonService.class);

        for (int i = 0; i < jsonArray.length(); i++) {
            InventoryDrug drug = null;
            JSONObject object = jsonArray.getJSONObject(i);
            Integer noOfDays = NumberUtils.toInt(object.getString("noOfDays"), 0);
            Integer formulation = NumberUtils.toInt(object.getString("drugPatientFormulationId"), 0);
            Integer frequency = NumberUtils.toInt(object.getString("drugPatientFrequencyId"), 0);
            int drugQuantity = NumberUtils.toInt(object.getString("drugQuantity"), 0);
            String drugIdStr = object.getString("drugId");
            String drugName = object.getString("drugPatientName");
            String comments = object.getString("issueComment");
            int category = NumberUtils.toInt(object.getString("issueDrugCategoryId"), 0);
            Concept freCon = conceptService.getConcept(frequency);
            InventoryStoreDrugTransactionDetail transactionDetail = inventoryService.getStoreDrugTransactionDetailById(object.getInt("id"));

            if (!drugName.equalsIgnoreCase("")) {
                drug = inventoryService.getDrugByName(drugName);
            } else if (!drugIdStr.equalsIgnoreCase("")) {
                int drugId = Integer.parseInt(drugIdStr);
                drug = inventoryService.getDrugById(drugId);
            }
            InventoryDrugFormulation formulationO = inventoryService.getDrugFormulationById(formulation);
            transactionDetail.setFrequency(freCon.getName().getConcept());
            transactionDetail.setNoOfDays(noOfDays.intValue());
            transactionDetail.setComments(comments);
            InventoryStoreDrugPatientDetail issueDrugDetail = new InventoryStoreDrugPatientDetail();
            issueDrugDetail.setTransactionDetail(transactionDetail);
            issueDrugDetail.setQuantity(drugQuantity);
            list.add(issueDrugDetail);
        }
        if (issueDrugPatient != null && list != null && list.size() > 0) {
            Date date = new Date();
            // create transaction issue from substore
            InventoryStoreDrugTransaction transaction = new InventoryStoreDrugTransaction();
            transaction.setDescription("ISSUE DRUG TO PATIENT "
                    + DateUtils.getDDMMYYYY());
            transaction.setStore(subStore);
            transaction.setTypeTransaction(ActionValue.TRANSACTION[1]);
            transaction.setCreatedOn(date);
            //transaction.setPaymentMode(paymentMode);
            transaction.setPaymentCategory(issueDrugPatient.getPatient().getAttribute(14).getValue());
            transaction.setCreatedBy(Context.getAuthenticatedUser().getGivenName());
            transaction = inventoryService.saveStoreDrugTransaction(transaction);
            issueDrugPatient = inventoryService.saveStoreDrugPatient(issueDrugPatient);
            for (InventoryStoreDrugPatientDetail pDetail : list) {
                Date date1 = new Date();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Integer totalQuantity = inventoryService.sumCurrentQuantityDrugOfStore(subStore.getId(), pDetail
                        .getTransactionDetail().getDrug().getId(), pDetail.getTransactionDetail().getFormulation().getId());
                int t = totalQuantity;
                InventoryStoreDrugTransactionDetail drugTransactionDetail = inventoryService.getStoreDrugTransactionDetailById(pDetail
                        .getTransactionDetail().getId());
                pDetail.getTransactionDetail().setCurrentQuantity(drugTransactionDetail.getCurrentQuantity());
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
                transDetail.setUnitPrice(pDetail.getTransactionDetail().getUnitPrice());
                transDetail.setDrug(pDetail.getTransactionDetail().getDrug());
                transDetail.setFormulation(pDetail.getTransactionDetail().getFormulation());
                transDetail.setBatchNo(pDetail.getTransactionDetail().getBatchNo());
                transDetail.setCompanyName(pDetail.getTransactionDetail().getCompanyName());
                transDetail.setDateManufacture(pDetail.getTransactionDetail().getDateManufacture());
                transDetail.setDateExpiry(pDetail.getTransactionDetail().getDateExpiry());
                transDetail.setReceiptDate(pDetail.getTransactionDetail().getReceiptDate());
                transDetail.setCreatedOn(date1);
                transDetail.setReorderPoint(pDetail.getTransactionDetail().getDrug().getReorderQty());
                transDetail.setAttribute(pDetail.getTransactionDetail().getDrug().getAttributeName());
                transDetail.setPatientType(patientType);

                transDetail.setFrequency(pDetail.getTransactionDetail().getFrequency());
                transDetail.setNoOfDays(pDetail.getTransactionDetail().getNoOfDays());
                transDetail.setComments(pDetail.getTransactionDetail().getComments());
                BigDecimal moneyUnitPrice = pDetail.getTransactionDetail().getCostToPatient()
                        .multiply(new BigDecimal(pDetail.getQuantity()));
                transDetail.setTotalPrice(moneyUnitPrice);
                transDetail.setParent(pDetail.getTransactionDetail());
                transDetail = inventoryService.saveStoreDrugTransactionDetail(transDetail);

                pDetail.setStoreDrugPatient(issueDrugPatient);
                pDetail.setTransactionDetail(transDetail);
                // save issue to patient detail
                inventoryService.saveStoreDrugPatientDetail(pDetail);
                // save issues transaction detail
            }

        }


        return "success";
    }

    public String processIssueDrugForIpdPatient() {
        throw new NotYetImplementedException("Not Yet Implemented for IPD");
    }


}
