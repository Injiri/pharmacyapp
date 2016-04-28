package org.openmrs.module.pharmacyapp.page.controller;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.Role;
import org.openmrs.api.context.Context;
import org.openmrs.module.hospitalcore.HospitalCoreService;
import org.openmrs.module.hospitalcore.model.InventoryDrug;
import org.openmrs.module.hospitalcore.model.InventoryDrugCategory;
import org.openmrs.module.hospitalcore.model.InventoryStore;
import org.openmrs.module.hospitalcore.model.InventoryStoreRoleRelation;
import org.openmrs.module.inventory.InventoryService;
import org.openmrs.module.inventory.model.InventoryStoreDrugIndentDetail;
import org.openmrs.module.pharmacyapp.StoreSingleton;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class IssueDrugPageController {
	public void controller(){
		}

	public void get
	(@RequestParam(value = "categoryId", required = false) Integer categoryId,
			@RequestParam(value="patientId",required=false) Integer patientId,
			
            PageModel model) {
		
		Patient Patient = Context.getPatientService().getPatient(patientId);
		InventoryService inventoryService = (InventoryService) Context.getService(InventoryService.class);
		HospitalCoreService hcs = Context.getService(HospitalCoreService.class);

		List<InventoryDrugCategory> listCategory = inventoryService.findDrugCategory("");
		model.addAttribute("listCategory", listCategory);
		model.addAttribute("categoryId", categoryId);
		if(categoryId != null && categoryId > 0){
		    List<InventoryDrug> drugs = inventoryService.findDrug(categoryId, null);
		    model.addAttribute("drugs",drugs);
		    
		
		}
		// InventoryStore store = inventoryService.getStoreByCollectionRole(new ArrayList<Role>(Context.getAuthenticatedUser().getAllRoles()));
		List <Role>role=new ArrayList<Role>(Context.getAuthenticatedUser().getAllRoles());
		
		InventoryStoreRoleRelation srl=null;
		Role rl = null;
		for(Role r: role){
		    if(inventoryService.getStoreRoleByName(r.toString())!=null){
		        srl = inventoryService.getStoreRoleByName(r.toString());
		        rl=r;
		    }
		}
		InventoryStore store =null;
		if(srl!=null){
		    store = inventoryService.getStoreById(srl.getStoreid());
		
		}
			model.addAttribute("store",store);
			model.addAttribute("date",new Date());
			model.addAttribute("patientId", patientId);
			model.addAttribute("identifier",Patient.getPatientIdentifier());
			model.addAttribute("category",Patient.getAttribute(14));
			model.addAttribute("age",Patient.getAge());
			model.addAttribute("birthdate",Patient.getBirthdate());
            model.addAttribute("lastVisit", hcs.getLastVisitTime(Patient));
            model.addAttribute("date", new Date());

			if (Patient.getGender().equals("M")){
				model.addAttribute("gender", "Male");
			}
			else{
				model.addAttribute("gender", "Female");
			}

			model.addAttribute("familyName",Patient.getFamilyName());
			model.addAttribute("givenName",Patient.getGivenName());

			if(Patient.getMiddleName() != null){
				model.addAttribute("middleName",Patient.getMiddleName());
			}
			else{
				model.addAttribute("middleName","");
			}

			int userId = Context.getAuthenticatedUser().getId();
			String fowardParam = "subStoreIndentDrug_"+userId;
			List<InventoryStoreDrugIndentDetail> list = (List<InventoryStoreDrugIndentDetail> ) StoreSingleton.getInstance().getHash().get(fowardParam);
			model.addAttribute("listIndent", list);
			
	}
}
