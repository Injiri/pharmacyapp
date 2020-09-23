<%
    ui.decorateWith("appui", "standardEmrPage", [title: "List of Orders"]);

    ui.includeJavascript("billingui", "moment.js")
%>
<script>
    jq(document).ready(function () {
        jq(".dashboard-tabs").tabs();

        jq('#surname').html(stringReplace('${patient.names.familyName}')+',<em>surname</em>');
        jq('#othname').html(stringReplace('${patient.names.givenName}')+' &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; <em>other names</em>');
        jq('#agename').html('${patient.age} years ('+ moment('${patient.birthdate}').format('DD,MMM YYYY') +')');
        jq('#lstdate').html('Last Visit: '+ moment('${previousVisit}').format('DD, MMM YYYY'));
    });
</script>

<style>
	.name {
		color: #f26522;
	}
	input[type="text"],
	input[type="password"],
	select {
		border: 1px solid #aaa;
		border-radius: 0px !important;
		box-shadow: none !important;
		box-sizing: border-box !important;
		height: 38px !important;
		line-height: 18px !important;
		padding: 8px 10px !important;
		width: 100% !important;
	}
	input[type="text"]:focus, textarea:focus{
		outline: 2px solid #007fff!important;
	}
	textarea{
		width: 97%;
	}
	.append-to-value{
		color: #999;
		float: right;
		left: auto;
		margin-left: -50px;
		margin-top: 5px;
		padding-right: 10px;
		position: relative;
	}
	form h2{
		margin: 10px 0 0;
		padding: 0 5px
	}
	.col1, .col2, .col3, .col4, .col5, .col6, .col7, .col8, .col9, .col10, .col11, .col12 {
		float: left;
	}
	form label, .form label {
		margin: 5px 0 0;
		padding: 0 5px
	}
	#datetime label{
		display: none;
	}
	#breadcrumbs a, #breadcrumbs a:link, #breadcrumbs a:visited {
		color: #555;
		text-decoration: none;
	}
	.add-on {
		float: right;
		left: auto;
		margin-left: -29px;
		margin-top: 10px;
		position: absolute;
	}
	.dashboard .info-section {
		margin: 0 5px 5px;
	}
	.dashboard .info-body li{
		padding-bottom: 2px;
	}

	.dashboard .info-body li span{
		margin-right:10px;
	}

	.dashboard .info-body li small{

	}

	.dashboard .info-body li div{
		width: 150px;
		display: inline-block;
	}
	.info-body ul li{
		display:none;
	}
	.simple-form-ui section.focused {
		width: 75%;
	}

	.new-patient-header .demographics .gender-age {
		font-size: 14px;
		margin-left: -55px;
		margin-top: 12px;
	}
	.new-patient-header .demographics .gender-age span {
		border-bottom: 1px none #ddd;
	}
	.new-patient-header .identifiers {
		margin-top: 10px;
	}
	
	.tag {
		padding: 2px 10px;
	}
	.tad {
		background: #666 none repeat scroll 0 0;
		border-radius: 1px;
		color: white;
		display: inline;
		font-size: 0.8em;
		padding: 2px 10px;
	}
	.status-container {
		padding: 5px 10px 5px 5px;
	}
	.catg{
		color: #363463;
		margin: 35px 10px 0 0;
	}
	.ui-tabs {
		margin-top: 5px;
	}
	.simple-form-ui section.focused {
		width: 74.6%;
		min-height: 400px;
	}
	.col15 {
		min-width: 22%;
		max-width: 22%;
		float: left;
		display: inline-block;
	}
	.col16 {
		min-width: 56%;
		max-width: 56%;
		float: left;
		display: inline-block;
	}
	.title{
		border: 	1px solid #eee;
		margin: 	3px 0;
		padding:	5px;
	}
	.title i{
		font-size: 1.5em;
		padding: 0;
	}
	.title span{
		font-size: 20px;
	}
	.title em{
		border-bottom: 1px solid #ddd;
		color: #888;
		display: inline-block;
		font-size: 0.5em;
		width: 200px;
	}
	th:first-child{
		width: 5px;
	}
	th:nth-child(2){
		width: 100px;
	}
	th:nth-child(3){
		width: 150px;
	}
	th:last-child{
		width: 135px;
	}
</style>

<div class="clear"></div>
<div id="content">
	<div class="example">
		<ul id="breadcrumbs">
			<li>
				<a href="${ui.pageLink('referenceapplication','home')}">
				<i class="icon-home small"></i></a>
			</li>
			
			<li>
				<i class="icon-chevron-right link"></i>
				<a href="${ui.pageLink('pharmacyapp','dashboard')}">Pharmacy</a>
			</li>
			
			<li>
				<i class="icon-chevron-right link"></i>
				<a href="${ui.pageLink('pharmacyapp','container',[rel:'patients-queue', date:date])}">Queue</a>
			</li>
			
			<li>
				<i class="icon-chevron-right link"></i>
				Orders
			</li>
		</ul>
	</div>
	
    <div class="patient-header new-patient-header">
        <div class="demographics">
            <h1 class="name">
                <span id="surname">${patient.names.familyName},<em>surname</em></span>
                <span id="othname">${patient.names.givenName} &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<em>other names</em></span>

                <span class="gender-age">
                    <span>
                        <% if (patient.gender == "F") { %>
                        Female
                        <% } else { %>
                        Male
                        <% } %>
                    </span>
                    <span id="agename">${patient.age} years (15.Oct.1996) </span>

                </span>
            </h1>

            <br/>
            <div id="stacont" class="status-container">
                <span class="status active"></span>
                Visit Status
            </div>
			<div class="tag">Outpatient</div>
            <div class="tad" id="lstdate">Last Visit: ${ui.formatDatePretty(previousVisit)}</div>
        </div>

        <div class="identifiers">
            <em>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;Patient ID</em>
            <span>${patient.getPatientIdentifier()}</span>
            <br>

            <div class="catg">
                <i class="icon-tags small" style="font-size: 16px"></i><small>Category:</small> ${patientCategory}
            </div>
        </div>
    </div>
	
	<div class="close"></div>
	
	<div class="title">
		<i class="icon-time"></i>
		<span>${date} <em>&nbsp; order date</em></span>
	</div>
	
	<table>
		<thead>
			<tr role="row">
				<th>#</th>
				<th>ORDER#</th>
				<th>DATE</th>
				<th>SENT FROM</th>
				<th>SEND BY</th>
				<th>ACTION</th>
			</tr>
		</thead>
		
		<tbody role="alert" aria-live="polite" aria-relevant="all">
			<% if (listOfOrders != null || listOfOrders != "") { %>
				<% listOfOrders.eachWithIndex { order , idx->  %>
					<tr>
						<td>${idx+1}</td>
						<td>
							<a href="drugOrder.page?patientId=${patientId}&encounterId=${order.encounter.encounterId}&patientType=${patientType}&date=${date}">
								${order.encounter.encounterId}</a></td>
						<td>${ui.formatDatePretty(order.createdOn)}</td>
						<td>
							<% if (order.referralWardName != null && order.referralWardName != "" && order.referralWardName != "null") { %>
								${order.referralWardName}
							<%} else {%>
								N/A
							<%}%>
						</td>
            <td>
                ${order.creator}
            </td>
						<td align="center">
							<a class="button task" href="drugOrder.page?patientId=${patientId}&encounterId=${order.encounter.encounterId}&patientType=${patientType}&date=${date}">
								<i class="icon-signout"></i>
								PROCESS
							</a>
						</td>
					</tr>
				<%}%>
			<%}else{%>
				<tr align="center">
					<td colspan="6">No order found</td>
				</tr>
			<%}%>
		</tbody>
    </table>
</div>
