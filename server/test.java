 protected void generateSingleInvoice(OaDocument oaDocument, String language, int decimals, PrintInvoiceDto printInvoiceDto,
                                         boolean isBulkAttachment, boolean isCreditNoteForBulkAttachment,
                                         EntityManager em) throws EntityValidityException, EntityPersistenceException, DataValidityException, FormatError {
        //***** Some checks **********************************************************************************************************************************************
        if (isBulkAttachment && printInvoiceDto.getClass() != PrintBulkAttachmentDto.class) {
            throw new DataValidityException("", "<<FinancialAdministrator.generateSingleInvoice>> 'printInvoiceDto' object is not an instance of PrintBulkAttachmentDto.");
        }
        if (printInvoiceDto == null) {
            throw new DataValidityException("", "<<FinancialAdministrator.generateSingleInvoice>> 'printInvoiceDto' cannot be null.");
        }
        //****************************************************************************************************************************************************************
        //***** Utilities *******************************************************************************************************************************************************************************************************************************
        FinancialAdministratorUtils financialAdministratorUtils = (FinancialAdministratorUtils) GenericFactory.getEBookingSingletonInstance(FinancialAdministratorUtils.class.getPackage().getName(), FinancialAdministratorUtils.class.getSimpleName());
        PaymentReferenceUtility paymentReferenceUtility = GenericFactory.getEBookingInstance(PaymentReferenceUtility.class);
        SimpleDateFormat formatter = new SimpleDateFormat(PatternEnumeration.DATE_FORMAT_DD_MM_YYYY.getValue());
        //***********************************************************************************************************************************************************************************************************************************************
        String invoiceNumber = oaDocument.getDocumentnumber();
        BigDecimal invoiceID = oaDocument.getOaDocumentPK().getDocumentid();
        //***** The map containing all the parameters of the report *****
        HashMap hm = new HashMap();
        //***************************************************************
        Collection<String> commissionsVatCodes = new ArrayList<>();
        //****************************************
        //***** The Query objects *************
        Query q;
        List<List<Object>> queryResult;
        //*************************************
        //***** Initialize the list of routes ********************************
        printInvoiceDto.setInvoiceRouteList(new ArrayList<>());
        //********************************************************************
        //***** Retrieves the Header and Footer info ****************************************************
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT D.CONNECTEDDOCUMENTID, ");           //0
        sb.append("        D.CONNECTEDDOCUMENTSTARTDATE, ");    //1
        sb.append("        D.DOCUMENTDATE, ");                  //2
        sb.append("        D.PAYMENTDUEDATE, ");                //3
        sb.append("        D.PAYMENTREFERENCE, ");              //4
        sb.append("        D.COMPANYNAME, ");                   //5
        sb.append("        D.GEOGRAPHICALLOCATIONNAME, ");      //6
        sb.append("        D.DIVISIONNAME, ");                  //7
        sb.append("        D.CURRENCYNAME, ");                  //8
        sb.append("        D.CUSTOMERID, ");                    //9
        sb.append("        D.STARTDATE, ");                     //10
        sb.append("        U.NAME, ");                          //11
        sb.append("        C.DESCRIPTION, ");                   //12
        sb.append("        D.AMOUNTPAID, ");                    //13
        sb.append("        DECODE(D.DOCUMENTTYPE,'INVOICE', -D.COMMISSIONAMOUNT, D.COMMISSIONAMOUNT) ");                //14
        sb.append("  FROM OA_DOCUMENT D, UA_USER U, UA_COMPANY C   ");
        sb.append("       WHERE D.DOCUMENTID = ? and D.USERSTARTDATE = U.STARTDATE  ");
        sb.append("       AND D.USERNAME = U.USERNAME  ");
        sb.append("       AND D.ENDDATE='99991231000000' ");
        sb.append("       AND D.COMPANYNAME = C.NAME ");
        sb.append("       AND D.COMPANYSTARDATE = C.STARDATE ");
        q = em.createNativeQuery(sb.toString());
        q.setParameter(1, invoiceID);
        Date start = new Date();
        List<?> rec = (List<?>) q.getSingleResult();
//        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> Retrieves the Header and Footer info: " + (DateUtils.getElapsedTime(start, new Date())));
        final BigDecimal connectedDocumentId = (BigDecimal) rec.get(0);
        final String connectedDocumentStartDate = (String) rec.get(1);
        final Date documentDate = (Date) rec.get(2);
        final Date paymentDueDate = (Date) rec.get(3);
        final String paymentReference = (String) rec.get(4);
        final String companyName = (String) rec.get(5);
        final String geographicalLocationName = (String) rec.get(6);
        final String divisionName = (String) rec.get(7);
        final String currencyName = (String) rec.get(8);
        final BigDecimal customerID = (BigDecimal) rec.get(9);
        final String documentStartDate = (String) rec.get(10);
        final String issuedByUserName = (String) rec.get(11);
        String companyDescription = (String) rec.get(12);//GRIM-4946: FS-576 - Invoice Text Changes for Trading unit FL DEU GmbH
        BigDecimal amountPaid = (BigDecimal) rec.get(13);
        //***********************************************************************************************
        //***** Set the document start date ********************
        printInvoiceDto.setDocumentStartDate(documentStartDate);
        //******************************************************
        //***** Get the Logo of the document *****
        LOGO = financialAdministratorUtils.getLogo(divisionName);
        hm.put("imageName", LOGO);
        //****************************************
        //***** Set the document date *****************
        hm.put("date", formatter.format(documentDate));
        //*********************************************
        //***** Set the due date *******************************
        if (paymentDueDate == null) {
            hm.put("dueDate", "");
        } else {
            hm.put("dueDate", formatter.format(paymentDueDate));
        }
        //******************************************************
        //***** Set Company details ************************************************************************************
        //start GRIM-4946: FS-576 - Invoice Text Changes for Trading unit FL DEU GmbH
        //***** The flag indicating if the document is a credit note *****
        boolean isCreditNote = (connectedDocumentId != null);
        //****************************************************************
        UaCompany uaCompany = oaDocument.getUaBusinessunit().getUaCompany();
        companyDescription = financialAdministratorUtils.customizeCompanyDescription(companyName, companyDescription);
        hm.put("companyName", companyDescription);
        hm.put("currency", currencyName);
        // START JIRA GRIM-5371
        EnterpriseAdministrator enterpriseAdministrator = GenericFactory.getEBookingInstance(EnterpriseAdministrator.class);
        InfoCompanyDTO oaCompanyAddress = enterpriseAdministrator.fillCompanyAddress(uaCompany);
        // END
        StringBuilder companyInfo = new StringBuilder();
        companyInfo.append("<html>");
        String customCompanyInfo = financialAdministratorUtils.getCompanyInfo(language, companyName, oaCompanyAddress); // GRIM-5371
        String otherCompanyInfo = financialAdministratorUtils.getOtherInfoOnCompany(companyName, language, isCreditNote);
        companyInfo.append(customCompanyInfo).append(otherCompanyInfo);
        companyInfo.append("</html>");
        hm.put("companyInfo", companyInfo.toString());
        //end GRIM-4946: FS-576 - Invoice Text Changes for Trading unit FL DEU GmbH
//        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> Set Company details: " + (DateUtils.getElapsedTime(start, new Date())));
        //**************************************************************************************************************
        //***** Set the user that is issuing the document *****
        hm.put("issuedBy", issuedByUserName);
        //*****************************************************
        //***** Get the print format of the payment reference *******************************
//        start = new Date();
        hm.put("paymentReference", paymentReferenceUtility.getPrintFormat(paymentReference, divisionName));
//        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> paymentReferenceUtility.getPrintFormat: " + (DateUtils.getElapsedTime(start, new Date())));
        //***********************************************************************************
        //start GRIM-4946: FS-576 - Invoice Text Changes for Trading unit FL DEU GmbH
        String asAgent = financialAdministratorUtils.getAsAgentValue(language, companyName);
        hm.put("asAgent", asAgent);
        //end GRIM-4946: FS-576 - Invoice Text Changes for Trading unit FL DEU GmbH
        //***** The flag indicating if the lines are for a modified booking in bulk invoice *****
        boolean isBulkAmendment = false;
        //***************************************************************************************
        //***** The flag indicating if the lines are for a deleted element to be credited (change customer functionality) *****
        boolean isDeletedElement = false;
        //*********************************************************************************************************************
        //***** The flag used to indicate that this line of invoice represent an element for which during change customer the price is changed *****
        boolean isChangedPrice = false;
        //*****************************************************************************************************************************************
        //***** The flag used to indicate that the price is less than the price of corresponding product in the joined booking *****
        boolean isPriceDecreased = false;
        //**************************************************************************************************************************
        //***** The flag used to indicate if the element is a rebate *****
        boolean isRebate;
        //****************************************************************
        //***** The flag used to indicate if the element is a surcharge *****
        boolean isSurcharge;
        //****************************************************************
        //***** Get Customer details *****************************************************************************************
//        start = new Date();
        q = em.createNamedQuery("selectLatestCustomerByAccountId");
        q.setParameter("accountId", customerID);
        q.setParameter("currentDate", DateUtils.getDateFormatted(new Date()));
        AccAccount accAccount = (AccAccount) q.getSingleResult();
        AccAccountdetail accAccountdetail = null;
        for (AccAccountdetail tmpDetail : accAccount.getAccAccountdetailCollection()) {
            if (tmpDetail.getUaCompany().getUaCompanyPK().equals(uaCompany.getUaCompanyPK())
                    && tmpDetail.getEnddate().equals(DateUtils.getDefaultEndDateString())) {
                accAccountdetail = tmpDetail;
                break;
            }
        }
        String contactEmail = "";
        String contactTelephone = "";
        if (accAccountdetail != null && accAccountdetail.getAccContactaccountdetailCollection() != null) {
            for (AccContactaccountdetail tmpContactDetail : accAccountdetail.getAccContactaccountdetailCollection()) {
                if (tmpContactDetail.getEnddate().equals(DateUtils.getDefaultEndDateString())) {
                    if (tmpContactDetail.getEmail() != null && contactEmail.length() == 0) {
                        contactEmail = tmpContactDetail.getEmail();
                    }
                    if (tmpContactDetail.getTelephone() != null && contactTelephone.length() == 0) {
                        contactTelephone = tmpContactDetail.getTelephone() + " ";
                    }
                }
                if (contactEmail.length() > 0 && contactTelephone.length() > 0) {
                    break;
                }
            }
        }
        hm.put("contactInfo", contactTelephone + contactEmail);
        financialAdministratorUtils.addCustomerId(hm, divisionName, customerID);
        hm.put("customerName", accAccount.getCustomername());
        hm.put("customerAddress", getMultilineString(accAccount.getCustomeraddress()));
        hm.put("customerVat", accAccount.getVatnumber());
        hm.put("customerPostCode", accAccount.getCustomerpostcode());
        hm.put("customerTown", accAccount.getCustomertown());
        hm.put("customerCountry", accAccount.getCountry().getName());
        hm.put("customerPoBox", accAccount.getCustomerpobox());
        hm.put("customerPoBoxCode", accAccount.getCustomerpoboxcode());
        hm.put("customerPoBoxTown", accAccount.getCustomerpotown());
        //START GRIM-96
        hm.put("permanentAddress", accAccount.getPermanentAddress());
        //END GRIM-96
        AccFinancialdetail accFinancialdetail = getAccFinancialDetail(customerID.toBigInteger(), accAccount.getAccAccountPK().getAccountversion(),
                companyName, geographicalLocationName, divisionName, em);
        // START GRIM-2576
        boolean invoiceToBookingholder;
        if (accFinancialdetail != null) {
            hm.put("printCustomer", (Boolean) !InvoiceToEnum.BOOKING_HOLDER.getValue().equals(accFinancialdetail.getInvoiceto()));
            //        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> Get Customer details: " + (DateUtils.getElapsedTime(start, new Date())));
            //********************************************************************************************************************
            invoiceToBookingholder = InvoiceToEnum.BOOKING_HOLDER.getValue().equals(accFinancialdetail.getInvoiceto());
        } else {
            invoiceToBookingholder = false;
        }
        // END GRIM-2576
        //***** Retrieves the document info ***************************************************************
//        start = new Date();
        sb = new StringBuilder();
        sb.append("SELECT IB.BOOKINGREFERENCE, ");  //0
        sb.append("       IB.TICKETNUMBER, ");      //1
        sb.append("       IBH.NAME, ");             //2
        sb.append("       IBH.SURNAME, ");          //3
        sb.append("       IBH.ADDRESS, ");          //4
        sb.append("       IB.PAYMENTMETHOD, ");     //5
        sb.append("       IB.HOLDERCONTACTID, ");   //6
        sb.append("       IBH.VATNUMBER, ");        //7
        sb.append("       IB.BOOKINGVERSION, ");    //8
        sb.append("       IB.STARTDATE, ");         //9
        sb.append("       IBH.TITLE, ");            //10
        //START GRIM-925: Failed to generate PDF report in "Booking Result" window
        sb.append("       IBH.POSTALZIPCODE, ");         //11
        //END GRIM-925: Failed to generate PDF report in "Booking Result" window
        sb.append("       IBH.TOWN, ");             //12
        sb.append("       IBH.COUNTRY,   ");        //13
        sb.append("       DOC.HOLDERCONTACTID, ");    //14
        sb.append("       IB.ACCOUNTID, ");    //15
        sb.append("       ACC.CUSTOMERNAME, ");    //16
        sb.append("       ACC.CUSTOMERADDRESS, ");    //17
        sb.append("       ACC.CUSTOMERPOSTCODE, ");    //18
        sb.append("       ACC.CUSTOMERTOWN, ");    //19
        sb.append("       ACC.CUSTOMERCOUNTRY, ");    //20
        sb.append("       ACC.CUSTOMERPOBOX, ");    //21
        sb.append("       ACC.CUSTOMERPOBOXCODE, ");    //22
        sb.append("       ACC.CUSTOMERPOTOWN, ");    //23
        sb.append("       ACC.VATNUMBER, ");    //24
        sb.append("       DOC.HOLDERVERSION, ");    //25
        sb.append("       IBH.COMPANY, ");    //26
        sb.append("       IB.BOOKINGDATE, ");    //27
        sb.append("       IB.PAYMENTTYPE ");    //28
        sb.append("FROM OA_DOCUMENT DOC,OA_DOCUMENTBOOKING DOCBOOK,IBE_BOOKING IB,IBE_BOOKINGHOLDER IBH, ACC_ACCOUNT ACC ");
        sb.append("WHERE DOC.DOCUMENTID = DOCBOOK.DOCUMENTID ");
        sb.append("AND DOC.STARTDATE = DOCBOOK.DOCUMENTSTARTDATE ");
        sb.append("AND DOCBOOK.BOOKINGREFERENCE = IB.BOOKINGREFERENCE ");
        sb.append("AND DOCBOOK.BOOKINGVERSION = IB.BOOKINGVERSION ");
        sb.append("AND DOCBOOK.BOOKINGSTARTDATE = IB.STARTDATE ");
        sb.append("AND IB.HOLDERCONTACTID = IBH.CONTACTID ");
        sb.append("AND IB.HOLDERVERSION = IBH.BOOKINGHOLDERVERSION ");
        sb.append("AND DOC.DOCUMENTID = ? ");
        sb.append("AND DOC.STARTDATE = ? ");
        sb.append("AND ACC.ACCOUNTID = IB.ACCOUNTID ");
        sb.append("AND ACC.ACCOUNTVERSION = IB.ACCOUNTVERSION ");
        if (isBulkAttachment) {
            sb.append(" AND IB.BOOKINGREFERENCE = ? ");
            sb.append(" AND IB.BOOKINGVERSION = ? ");
        }
        q = em.createNativeQuery(sb.toString());
        if (connectedDocumentId == null) {
            q.setParameter(1, invoiceID);
            q.setParameter(2, documentStartDate);
        } else {
            q.setParameter(1, connectedDocumentId);
            q.setParameter(2, connectedDocumentStartDate);
            isCreditNote = true;
        }
        if (isBulkAttachment) {
            q.setParameter(3, ((PrintBulkAttachmentDto) printInvoiceDto).getBookingReference());
            q.setParameter(4, ((PrintBulkAttachmentDto) printInvoiceDto).getBookingVersion());
        }
        if (isCreditNoteForBulkAttachment) {
            isCreditNote = true;
        }
        rec = (List<?>) q.getSingleResult();
        final String bookingReference = (String) rec.get(0);
        final String ticketNumber = (String) rec.get(1);
        String bookingHolderName = (String) rec.get(2);
        String bookingHolderSurname = (String) rec.get(3);
        String bookingHolderAddress = (String) rec.get(4);
        final String paymentMethod = (String) rec.get(5);
        String bookingHolderContactId = ((BigDecimal) rec.get(6)).toString();
        String bookingHolderVat = (String) rec.get(7);
        final BigDecimal bookingVersion = (BigDecimal) rec.get(8);
        final String bookingStartDate = (String) rec.get(9);
        String bookingHolderTitle = (String) rec.get(10);
        String bookingHolderPostCode = (String) rec.get(11);
        String bookingHolderTown = (String) rec.get(12);
        String bookingHolderCountry = (String) rec.get(13);
        final BigDecimal invoiceHolderID = (BigDecimal) rec.get(14);
        final BigDecimal invoiceHolderVersion = (BigDecimal) rec.get(25);
        String isCompanyBookingHolder = (String) rec.get(26);
        Date bookingDate = (Date) rec.get(27);
        String paymentType = (String) rec.get(28);
//        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> Retrieves the document info: " + (DateUtils.getElapsedTime(start, new Date())));
        //***** Used to convert the amounts of the product of the joined booking (if any) ****************************************
//        start = new Date();
        /*BigDecimal exchangeRateForJoined = */
        BookingUtils.getExchangeRateForJoined(bookingReference, bookingVersion.intValue(), em);
//        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> getExchangeRateForJoined: " + (DateUtils.getElapsedTime(start, new Date())));
        //************************************************************************************************************************
//        start = new Date();
        Long customerIdBookingOperatorId = new CustomerAccountAdministrator().getCustomerIdBookingOperatorId(bookingReference,
                BigInteger.valueOf(bookingVersion.longValue()), em);
//        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> getCustomerIdBookingOperatorId: " + (DateUtils.getElapsedTime(start, new Date())));
        BigDecimal customerIdBookingOperatorIdBigDecimal = BigDecimal.ZERO;
        if (customerIdBookingOperatorId != null) {
            customerIdBookingOperatorIdBigDecimal = BigDecimal.valueOf(customerIdBookingOperatorId);
        }
        //***** Set the customer details if this is a bulk attachment ***************************************
//        start = new Date();
        if (isBulkAttachment) {
            financialAdministratorUtils.setBulkAttachmentCustomerDetails(divisionName, printInvoiceDto, rec);
        }
//        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> setBulkAttachmentCustomerDetails: " + (DateUtils.getElapsedTime(start, new Date())));
        //***************************************************************************************************
        //***** Set other company info *****************************************************************************************
//        start = new Date();
        hm.put("otherCompanyInfo", financialAdministratorUtils.getOtherInfoOnCompany(companyName, language, isCreditNote));
//        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> getOtherInfoOnCompany: " + (DateUtils.getElapsedTime(start, new Date())));
        //**********************************************************************************************************************
        hm.put("isCreditNote", isCreditNote);
        hm.put("bookingReference", bookingReference);
        hm.put("ticketNumber", ticketNumber);
        hm.put("invoiceNumber", invoiceNumber);
        hm.put("paymentMethod", paymentMethod);
//        start = new Date();
        hm.put("bankAccount", financialAdministratorUtils.getBankStatement(uaCompany.getOaBankCollection(), currencyName, divisionName, language, customerIdBookingOperatorIdBigDecimal, paymentMethod, bookingDate, paymentType));
//        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> getBankStatement: " + (DateUtils.getElapsedTime(start, new Date())));
        if ("Y".equalsIgnoreCase(isCompanyBookingHolder)) {
            bookingHolderSurname = "";
        }
//        start = new Date();
        if (invoiceToBookingholder && invoiceHolderID != null) {
            BookingHolderAdministrator bookingHolderAdministrator = new BookingHolderAdministrator();
            IbeBookingholder invoiceHolder = bookingHolderAdministrator.retrieveIbeBookingholderByContactIdAndVersion(invoiceHolderID.toString(),
                    invoiceHolderVersion.toString(), em);
            bookingHolderContactId = invoiceHolderID.toString();
            bookingHolderVat = invoiceHolder.getVatNumber();
            bookingHolderTitle = invoiceHolder.getTitle() != null ? invoiceHolder.getTitle().getCode() : "";
            bookingHolderPostCode = invoiceHolder.getPostalzipcode();//BCFER-202: BCF.GAP.APC - 065 AP Cheque
            bookingHolderTown = invoiceHolder.getTown();
            bookingHolderCountry = invoiceHolder.getCountry();
            bookingHolderName = invoiceHolder.getName();
            if ("Y".equalsIgnoreCase(invoiceHolder.getCompany())) {
                bookingHolderSurname = "";
            } else {
                bookingHolderSurname = invoiceHolder.getSurname();
            }
            bookingHolderAddress = invoiceHolder.getAddress();
        }
//        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> retrieveIbeBookingholderByContactIdAndVersion: " + (DateUtils.getElapsedTime(start, new Date())));
        String bhName = getBookingHolderNameString(bookingHolderName, bookingHolderSurname); // CFL-20
        String bhAddress = getMultilineString(bookingHolderAddress);
        hm.put("bookingHolderName", bhName);
        hm.put("bookingHolderAddress", bhAddress);
        hm.put("bookingHolderId", bookingHolderContactId);
        hm.put("bookingHolderVat", bookingHolderVat);
        hm.put("bookingHolderTitle", bookingHolderTitle);
        hm.put("bookingHolderPostCode", bookingHolderPostCode);
        hm.put("bookingHolderTown", bookingHolderTown);
        hm.put("bookingHolderCountry", bookingHolderCountry);
        StringBuilder queryString = new StringBuilder();
        StringBuilder innerQuery = new StringBuilder();
        StringBuilder innerQueryForJoined = new StringBuilder();
        queryString.append("SELECT  ROUTE, ");      //0
        queryString.append("DEP_DATE, ");           //1
        queryString.append("P_TYPE, ");             //2
        queryString.append("SHIP_NAME, ");          //3
        queryString.append("IS_PRODUCT, ");         //4
        queryString.append("P_CODE, ");             //5
        queryString.append("P_NAME, ");             //6
        queryString.append("SUM(QUANTITY) AS quantity, ");  //7
        queryString.append("UNIT_PRICE, ");         //8
        queryString.append("SUM(NET_AMOUNT) AS netAmount, ");   //9
        queryString.append("SUM(VAT_AMOUNT) AS vatAmount, ");   //10
        queryString.append("SUM(TOTAL_AMOUNT) AS totalAmount, ");   //11
        queryString.append("SUM(DISCOUNT_AMOUNT) AS discountAmount, "); //12
        queryString.append("SUM(COMMISSION_AMOUNT) AS commissionAmount, "); //13
        queryString.append("VATCODE, ");            //14
        queryString.append("ARR_DATE, ");           //15
        queryString.append("BOOKINGREFERENCE, ");   //16
        queryString.append("SURCHARGEDESCRIPTION, ");   //17
        queryString.append("ISBULKAMENDMENT, ");    //18
        queryString.append("VATCOMMISSIONCODE, ");  //19
        queryString.append("SUM(COMMISSION_VAT_AMOUNT) AS COMMISSIONVATAMOUNT, ");  //20
        queryString.append("ISDELETEDELEMENT, ");   //21
        queryString.append("ISCHANGEDPRICE, ");     //22
        queryString.append("ISPRICEDECREASED, ");   //23
        queryString.append("SUM(DISCOUNT_NET_AMOUNT) AS discountNetAmount, ");  //24
        queryString.append("SUM(DISCOUNT_VAT_AMOUNT) AS discountVatAmount, ");  //25
        queryString.append("IS_REBATE, ");           //26
        queryString.append("IS_SURCHARGE, ");           //27
        // START JIRA GRIM-822
        queryString.append("FLEX_LEVEL, "); //28
        // END
        // START JIRA CFL-20
        manageAdditionalFieldsForAggregation(queryString, false); //CFL-20
//        queryString.append("CUSTOMERREFERENCE, ");  //29
//        queryString.append("VEHICLELENGTH,  ");     //30
//        queryString.append("VEHICLEREGNUM,  ");     //31
//        queryString.append("DEPARTUREPORTNAME, ");     //32
//        queryString.append("ARRIVALPORTNAME,   ");  //33
//        queryString.append("PRODUCTINDEX, 			 "); //34
//        queryString.append("PRODUNITDISCOUNTAMOUNT,    "); //35
//        queryString.append("PRODUNITDISCOUNTVALUE,     "); //36
//        queryString.append("PRODDISCOUNTCALCMETHOD,    "); //37
//        queryString.append("TOTBPDISCOUNTAMOUNT,       "); //38
//        queryString.append("COUNTBPDISCOUNTAPPLIED,    "); //39
//        queryString.append("AVGBPDISCOUNTAVALUE,       "); //40
//        queryString.append("MAXBPDISCOUNTCALCMETHOD,   "); //41
//        queryString.append("TOTBPGENDISCOUNTAMOUNT,    "); //42
//        queryString.append("COUNTBPGENDISCOUNTAPPLIED, "); //43
//        queryString.append("AVGBPGENDISCOUNTAVALUE,    "); //44
//        queryString.append("MAXBPGENDISCOUNTCALCMETHOD "); //45
//        queryString.append("VATDESCRIPTION "); //46

        // END
        queryString.append(", (SELECT t1.surname FROM IBE_BOOKEDPASSENGER t1 WHERE t1.BOOKINGREFERENCE = t2.bookingreference AND t1.BOOKINGVERSION = t2.Bookingversion AND t1.PASSENGERINDEX = t2.productindex) AS passengersurname ");
        queryString.append(", VATRATE, ");
        queryString.append(" SUM(CASE ")
                .append("WHEN VATCODE = 'FR_ZERO_RATE_VAT' THEN 0 ")
                .append("WHEN VATCODE = 'FR_STD_RATE_VAT' THEN TOTAL_AMOUNT * (VATRATE / 100) ")
                .append("ELSE TOTAL_AMOUNT * (VATRATE / 100) ")
                .append("END) AS VATAMOUNTWITHOUTROUNDINGOFF, ");
        queryString.append("CASE ");
        queryString.append("WHEN SUM(DISCOUNT_AMOUNT) < SUM(UNIT_PRICE) THEN SUM(DISCOUNT_AMOUNT) ");
        queryString.append("ELSE SUM(UNIT_PRICE) ");
        queryString.append("END AS ACTUALDISCOUNTAMOUNT  ");
        queryString.append("FROM ( ");
        initInnerQueryForInvoiceLines(innerQuery, decimals);
        if (isBulkAttachment) {
            innerQuery.append("AND (ILV.BOOKINGREFERENCE,ILV.BOOKINGVERSION) IN ((?bookingreference, ?bookingversion))");
            //needed to avoid problems for invoices invoicing both joined and linked booking. For new booking the deleted element in bulk attachment must not be included
            innerQuery.append("AND ILV.ISDELETEDELEMENT = 'N' ");
            if (StringUtils.isNotEmpty(((PrintBulkAttachmentDto) printInvoiceDto).getJoinedBookingReference())) {
                innerQueryForJoined.append(" UNION ALL ");
                initInnerQueryForInvoiceLines(innerQueryForJoined, decimals);
                innerQueryForJoined.append("AND (ILV.BOOKINGREFERENCE,ILV.BOOKINGVERSION) IN ((?joinedbookingreference, ?joinedbookingversion) ) ");
                //needed to avoid problems for invoices invoicing both joined and linked booking. For joined booking must be considered only the deleted elements in bulk attachment
                innerQueryForJoined.append("AND ILV.ISDELETEDELEMENT = 'Y' ");
            }
        }
        queryString.append(innerQuery);
        queryString.append(innerQueryForJoined);
        queryString.append(") t2 ");
        queryString.append("GROUP BY ROUTE, DEP_DATE, SHIP_NAME, P_TYPE, P_CODE, P_NAME, UNIT_PRICE, COMMISSION_AMOUNT, ROUTE_NET_AMOUNT, ROUTE_VAT_AMOUNT, ");
        queryString.append("ROUTE_TOT_AMOUNT, VATCODE, ARR_DATE, IS_PRODUCT,BOOKINGREFERENCE, SURCHARGEDESCRIPTION, ISBULKAMENDMENT, VATCOMMISSIONCODE, ");
        // START JIRA GRIM-822
        queryString.append("COMMISSION_VAT_AMOUNT, ISDELETEDELEMENT, ISCHANGEDPRICE, ISPRICEDECREASED, IS_REBATE, IS_SURCHARGE, FLEX_LEVEL, BOOKINGVERSION, ");
        // END
        manageAdditionalFieldsForAggregation(queryString, false); //CFL-20
        queryString.append(" ,VATRATE  ");
        queryString.append("ORDER BY DEP_DATE, ROUTE, P_TYPE, IS_PRODUCT, P_CODE");
        q = em.createNativeQuery(queryString.toString());
        q.setParameter("documentid", invoiceID);
        if (isBulkAttachment) {
            q.setParameter("bookingreference", ((PrintBulkAttachmentDto) printInvoiceDto).getBookingReference());
            q.setParameter("bookingversion", ((PrintBulkAttachmentDto) printInvoiceDto).getBookingVersion());
            if (StringUtils.isNotEmpty(((PrintBulkAttachmentDto) printInvoiceDto).getJoinedBookingReference())) {
                q.setParameter("joinedbookingreference", ((PrintBulkAttachmentDto) printInvoiceDto).getJoinedBookingReference());
                q.setParameter("joinedbookingversion", ((PrintBulkAttachmentDto) printInvoiceDto).getJoinedBookingVersion());
            }
        }
//        start = new Date();
        queryResult = q.getResultList();
        // reOrderLists(queryResult);
        LOGGER.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> query invoice lines (OA_INVOICELINESVIEW): " + (DateUtils.getElapsedTime(start, new Date())));
        //**********************************************************************************************************************************************************************************************************
        boolean routeChanged = false;
//        start = new Date();
        final Map<String, Map<String, String>> productLanguagesMap = new ProductAdministrator().fillDataCacheProductMap(em);    // GRIM-3160
//        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> fillDataCacheProductMap: " + (DateUtils.getElapsedTime(start, new Date())));
        String key = language + "-" + divisionName;
        final Map<String, String> productLanguageMap = productLanguagesMap.get(key);    // GRIM-3160
        ArrayList<String> routes = new ArrayList<>();
        String currentRouteStr = " ";
        String currentRowStr = " ";
        Date currentDepDate = new Date(0);
        Date currentArrDate = new Date(0);
        InvoiceRouteDto tempRouteDto = null;
        InvoiceRowDto tempRowDto = null;
        InvoiceProductDto tempProductDto;
        Collection<String> productTypes = new ArrayList<>();
        for (Object rowObj : queryResult) {
            List<?> row = (List<?>) rowObj;
            final String routeName = (String) row.get(0);
            final Date departureDate = (Date) row.get(1);
            final String productType = (String) row.get(2);
            if (productType == null) {
                continue;
            }
            if (!productTypes.contains(productType)) {
                productTypes.add(productType);
            }
            final String shipName = (String) row.get(3);
            final boolean isProduct = row.get(4).equals("0");
            final String productCode = (String) row.get(5);
            final Object productQuantity = row.get(7);
            // START JIRA GRIM-822
            final String flexibilityLevel = (String) row.get(28);
            // END

            //START CFL-20
            final String customerReference = row.get(29) != null ? (String) row.get(29) : null;
            final BigDecimal vehicleLength = row.get(30)  != null ?  (BigDecimal) row.get(30) : null;
            final String vehicleRegNumber = row.get(31) != null ? (String) row.get(31) : null;
            final String departurePortName =row.get(32) != null ?  (String) row.get(32) : null;
            final String arrivalPortName =row.get(33) != null ?  (String) row.get(33) : null;
            String passengerSurname = (String) row.get(47);
            //END
            if (StringUtils.isEmpty((String) row.get(18))) {
                isBulkAmendment = StringUtils.getTrueOrFalse("N");
            } else {
                final String isBulkAmendmentString = (String) row.get(18);
                isBulkAmendment = StringUtils.getTrueOrFalse(isBulkAmendmentString);
            }
            // START GRIM-2780
            String vatCommissionCodes = (String) row.get(19);
            try {
                if (vatCommissionCodes != null && !StringUtils.isEmpty(vatCommissionCodes)) {
                    VatEngine vatEngine = new VatEngine();
                    String[] vatNamesSplitted = vatCommissionCodes.split(", ");
                    vatCommissionCodes = "";
                    for (String vatRuleName : vatNamesSplitted) {
                        String vatCode = vatEngine.findVatCodeByVatName(vatRuleName, em);
                        vatCommissionCodes = vatCommissionCodes.concat(vatCode + ",");
                    }
                    vatCommissionCodes = vatCommissionCodes.substring(0, vatCommissionCodes.length() - 1);
                }
            } catch (EntityValidityException | EntityPersistenceException e) {
                java.util.logging.Logger.getLogger(FinancialAdministrator.class.getName()).log(Level.SEVERE, null, e);
            }
            // END GRIM-2780
            if (StringUtils.isEmpty((String) row.get(21))) {
                isDeletedElement = StringUtils.getTrueOrFalse("N");
            } else {
                final String isDeletedElementString = (String) row.get(21);
                isDeletedElement = StringUtils.getTrueOrFalse(isDeletedElementString);
            }
            if (StringUtils.isEmpty((String) row.get(22))) {
                isChangedPrice = StringUtils.getTrueOrFalse("N");
            } else {
                final String isChangedPriceString = (String) row.get(22);
                isChangedPrice = StringUtils.getTrueOrFalse(isChangedPriceString);
            }
            if (StringUtils.isEmpty((String) row.get(23))) {
                isPriceDecreased = StringUtils.getTrueOrFalse("N");
            } else {
                final String isPriceDecreasedString = (String) row.get(23);
                isPriceDecreased = StringUtils.getTrueOrFalse(isPriceDecreasedString);
            }
            if (StringUtils.isEmpty((String) row.get(26))) {
                isRebate = StringUtils.getTrueOrFalse("N");
            } else {
                final String isRebateString = (String) row.get(26);
                isRebate = StringUtils.getTrueOrFalse(isRebateString);
            }
            if (StringUtils.isEmpty((String) row.get(27))) {
                isSurcharge = StringUtils.getTrueOrFalse("N");
            } else {
                final String isSurchargeString = (String) row.get(27);
                isSurcharge = StringUtils.getTrueOrFalse(isSurchargeString);
            }
            //***** Skip some elements added to manage the change customer and joined status *****************
            if (isBulkAttachment) {
                boolean bulkAmendmentBooking = ((PrintBulkAttachmentDto) printInvoiceDto).isIsBulkAmendment();
                if (!isBulkAmendment && isDeletedElement && bulkAmendmentBooking) {
                    continue;
                }
                if (!bulkAmendmentBooking && isDeletedElement && isBulkAmendment) {
                    continue;
                }
            }
            //************************************************************************************************
            // START GRIM-2780
            String productVatCodes = (String) row.get(14);
            try {
                if (productVatCodes != null && !StringUtils.isEmpty(productVatCodes)) {
                    VatEngine vatEngine = new VatEngine();
                    String[] vatNamesSplitted = productVatCodes.split(",");
                    productVatCodes = "";
                    for (String vatRuleName : vatNamesSplitted) {
                        String vatCode = vatEngine.findVatCodeByVatName(vatRuleName, em);
                        productVatCodes = productVatCodes.concat(vatCode + ",");
                    }
                    productVatCodes = productVatCodes.substring(0, productVatCodes.length() - 1);
                }
            } catch (EntityValidityException | EntityPersistenceException e) {
                java.util.logging.Logger.getLogger(FinancialAdministrator.class.getName()).log(Level.SEVERE, null, e);
            }
            // END GRIM-2780
            //***** unitPrice must be:
            //***** POSITIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** NEGATIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            BigDecimal unitPrice = (BigDecimal) row.get(8);
            boolean changedPriceForInvoice = isChangedPrice;
            if (unitPrice.compareTo(BigDecimal.ZERO) < 0 && isChangedPrice) {
                changedPriceForInvoice = false;
            }
            unitPrice = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    unitPrice,
                    false,
                    isDeletedElement,
                    changedPriceForInvoice,
                    isPriceDecreased,
                    isRebate,
                    isSurcharge);
            //***** productNetAmount must be:
            //***** POSITIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** NEGATIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            BigDecimal productNetAmount = (BigDecimal) row.get(9);
            if (productNetAmount.compareTo(BigDecimal.ZERO) < 0 && isChangedPrice) {
                changedPriceForInvoice = false;
            }
            productNetAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    productNetAmount,
                    false,
                    isDeletedElement,
                    changedPriceForInvoice,
                    isPriceDecreased,
                    isRebate,
                    isSurcharge);
            //***** productVatAmount must be:
            //***** POSITIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** NEGATIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            BigDecimal productVatAmount = (BigDecimal) row.get(10);
            BigDecimal productVatWithoutRoundOff= (BigDecimal) row.get(49);
            if (productVatAmount.compareTo(BigDecimal.ZERO) < 0 && isChangedPrice) {
                changedPriceForInvoice = false;
            }
            productVatAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    productVatAmount,
                    false,
                    isDeletedElement,
                    changedPriceForInvoice,
                    isPriceDecreased,
                    isRebate,
                    isSurcharge);

            productVatWithoutRoundOff = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    productVatWithoutRoundOff,
                    false,
                    isDeletedElement,
                    changedPriceForInvoice,
                    isPriceDecreased,
                    isRebate,
                    isSurcharge);
            //***** discountVatAmount must be:
            //***** POSITIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** NEGATIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            BigDecimal discountVatAmount = (BigDecimal) row.get(25);
            if (discountVatAmount.compareTo(BigDecimal.ZERO) < 0 && isChangedPrice) {
                changedPriceForInvoice = false;
            }
            discountVatAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    discountVatAmount,
                    false,
                    isDeletedElement,
                    changedPriceForInvoice,
                    isPriceDecreased,
                    isRebate,
                    isSurcharge);
            productVatAmount = calculateProductVatAmount(productVatAmount, discountVatAmount);
            productVatWithoutRoundOff=calculateProductVatAmount(productVatWithoutRoundOff, discountVatAmount);
            //***** productTotalAmount must be:
            //***** POSITIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** NEGATIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            BigDecimal productTotalAmount = (BigDecimal) row.get(11);
            if (productTotalAmount.compareTo(BigDecimal.ZERO) < 0 && isChangedPrice) {
                changedPriceForInvoice = false;
            }
            productTotalAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    productTotalAmount,
                    false,
                    isDeletedElement,
                    changedPriceForInvoice,
                    isPriceDecreased,
                    isRebate,
                    isSurcharge);
            //***** productDiscountAmount must be:
            //***** POSITIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** NEGATIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            BigDecimal productDiscountAmount = (BigDecimal) row.get(50);
            if (productDiscountAmount.compareTo(BigDecimal.ZERO) < 0 && isChangedPrice) {
                changedPriceForInvoice = false;
            }
            productDiscountAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    productDiscountAmount,
                    false,
                    isDeletedElement,
                    changedPriceForInvoice,
                    isPriceDecreased,
                    isRebate,
                    isSurcharge);
            //START CFL-20
            BigDecimal productDiscountPercentage = null;
            if(productDiscountAmount != null && productDiscountAmount.compareTo(BigDecimal.ZERO) != 0) {
                // if there is only 1 discount we retrieve the exact discount value for PERCENTAGE
                // if more than 1 discounts have been applied, the percentage will be calculated if needed
                BigDecimal standardDiscAmont =  row.get(35) != null ?       (BigDecimal) row.get(35) : BigDecimal.ZERO;
                BigDecimal standardProdDiscValue =         (BigDecimal) row.get(36);
                String standardDiscountMethod =         (String) row.get(37);
                BigDecimal totProdDiscAmont =         (BigDecimal) row.get(38);
                BigDecimal countProdDiscounts =    row.get(38) != null ?     (BigDecimal) row.get(39) : BigDecimal.ZERO;
                BigDecimal prodDiscValue =         (BigDecimal) row.get(40);
                String prodDiscountMethod =         (String) row.get(41);
                BigDecimal totGenericDiscAmont =         (BigDecimal) row.get(42);
                BigDecimal countGenericDiscounts =  row.get(42) != null ? (BigDecimal) row.get(43) : BigDecimal.ZERO;
                BigDecimal genericProdDiscValue =         (BigDecimal) row.get(44);
                String genericDiscountMethod =         (String) row.get(45);
                if(standardDiscAmont.compareTo(BigDecimal.ZERO) != 0 && (countProdDiscounts.add(countGenericDiscounts).equals(BigDecimal.ZERO))) {
                    //only product standar discount is applied
                    if("PERCENT".equalsIgnoreCase(standardDiscountMethod)) {

                        productDiscountPercentage = standardProdDiscValue;
                    }
                } else if(countProdDiscounts.add(countGenericDiscounts).equals(BigDecimal.ONE)) {
                    if(countProdDiscounts.equals(BigDecimal.ONE)) {
                        if("PERCENT".equalsIgnoreCase(prodDiscountMethod)) {
                            productDiscountPercentage = prodDiscValue;
                        }
                    } else {
                        if("PERCENT".equalsIgnoreCase(genericDiscountMethod)) {
                            productDiscountPercentage = genericProdDiscValue;
                        }
                    }
                }
            }
            String lineVatDescription = row.get(46) != null ? (String) row.get(46) : "";
            //END CFL-20
            BigDecimal productDiscountNetAmount = (BigDecimal) row.get(24);
            if (productDiscountNetAmount.compareTo(BigDecimal.ZERO) < 0 && isChangedPrice) {
                changedPriceForInvoice = false;
            }
            productDiscountNetAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    productDiscountNetAmount,
                    false,
                    isDeletedElement,
                    changedPriceForInvoice,
                    isPriceDecreased,
                    isRebate,
                    isSurcharge);
            //***** productCommissionAmount must be:
            //***** NEGATIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** POSITIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            BigDecimal productCommissionAmount = (BigDecimal) row.get(13);
            if (productCommissionAmount.compareTo(BigDecimal.ZERO) < 0 && isChangedPrice) {
                changedPriceForInvoice = false;
            }
            productCommissionAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    productCommissionAmount,
                    true,
                    isDeletedElement,
                    changedPriceForInvoice,
                    isPriceDecreased,
                    isRebate,
                    isSurcharge);
            //***** vatCommissionAmount must be:
            //***** NEGATIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** POSITIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            BigDecimal vatCommissionAmount = (BigDecimal) row.get(20);
            if (vatCommissionAmount.compareTo(BigDecimal.ZERO) < 0 && isChangedPrice) {
                changedPriceForInvoice = false;
            }
            vatCommissionAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    vatCommissionAmount,
                    true,
                    isDeletedElement,
                    changedPriceForInvoice,
                    isPriceDecreased,
                    isRebate,
                    isSurcharge);
            String surchargeDescription = (String) row.get(17);
            if (!currentRouteStr.equals(routeName) || DateUtils.compareDate(departureDate, currentDepDate) != 0) {
                // <editor-fold defaultstate="collapsed" desc="Route Section Creation">
                currentRouteStr = routeName;
                routes.add(routeName);
                currentDepDate = departureDate;
                currentArrDate = (Date) row.get(15);
                tempRouteDto = new InvoiceRouteDto();
                tempRouteDto.setInvoiceRowList(new ArrayList<>());
                tempRouteDto.setPrintBookingFee(new ArrayList<>());
                tempRouteDto.setPrintBookingSurcharge(new ArrayList<>());
                tempRouteDto.setDepartureDateAndTime(currentDepDate != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(currentDepDate) : null);
                tempRouteDto.setArrivalDateAndTime(currentDepDate != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(currentArrDate) : null);
                tempRouteDto.setShipName(shipName);
                tempRouteDto.setLineDescription(" ");
                tempRouteDto.setLegCode(retriveLegCodeExtended(currentRouteStr, em));
                // START JIRA GRIM-822
                tempRouteDto.setFlexibilityLevel(flexibilityLevel);
                // END
                //START CFL-20
                tempRouteDto.setArrivalPortDescription(arrivalPortName);
                tempRouteDto.setDeparturePortDescription(departurePortName);
                tempRouteDto.setCustomerReference(customerReference);
                // END
                printInvoiceDto.getInvoiceRouteList().add(tempRouteDto);
                routeChanged = true;
                // </editor-fold>
            }
            if (!currentRowStr.equals(productType) || routeChanged) {
                // <editor-fold defaultstate="collapsed" desc="Row Section Creation">
                currentRowStr = productType;
                tempRowDto = new InvoiceRowDto();
                tempRouteDto.getInvoiceRowList().add(tempRowDto);
                tempRowDto.setSubProductList(new ArrayList<>());
                tempRowDto.setProductType(currentRowStr);
                if ("zzzzzz".equals(currentRowStr)) {
                    tempRowDto.setProductType("Surcharge");//TODO: PIERO: internazionalizzare
                }
                routeChanged = false;
                // </editor-fold>
            }
            tempProductDto = new InvoiceProductDto();
            tempRowDto.getSubProductList().add(tempProductDto);
            if (isProduct) // Product
            {
                tempProductDto.setProductDesc(productLanguageMap.get(productCode));
                tempProductDto.setVehicleLength(vehicleLength);
                tempProductDto.setVehicleRegNumber(vehicleRegNumber);

            } else { //Surcharge
                tempProductDto.setProductDesc("Surcharge: " + surchargeDescription);
            }
            //START JIRA GRIM-37 | added customization for division
            setProductCodeForInvoice(tempProductDto, productLanguageMap, productCode, divisionName);
            //END
            tempProductDto.setProductQuantity(productQuantity == null ? " " : String.valueOf(row.get(7)));
            tempProductDto.setUnitPrice(MathUtil.toFixedDecimal((unitPrice).doubleValue(), decimals));
            tempProductDto.setAmount(MathUtil.toFixedDecimal((productNetAmount).doubleValue(), decimals));
            tempProductDto.setAmountWithoutRoundingOff(String.valueOf(productNetAmount.doubleValue()));
            tempProductDto.setVat(MathUtil.toFixedDecimal((productVatAmount).doubleValue(), decimals));
            tempProductDto.setVatWithoutRoundOff(productVatWithoutRoundOff,isCreditNote);
            tempProductDto.setVatCode(productVatCodes);
            tempProductDto.setVatDescription(lineVatDescription);
            tempProductDto.setTotal(MathUtil.toFixedDecimal((productTotalAmount).doubleValue(), decimals));
            BigDecimal discountToShow = getDiscountToShow(productDiscountAmount, productDiscountNetAmount);
            tempProductDto.setDiscount(MathUtil.toFixedDecimal((discountToShow).doubleValue(), decimals));
            if(productDiscountAmount.abs() .compareTo(unitPrice.abs()) < 0){
            BigDecimal percentage= productDiscountAmount.divide(unitPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            tempProductDto.setDiscountToShow(MathUtil.toFixedDecimal((percentage).doubleValue(), 1));
            }
            tempProductDto.setDiscountPercentage(productDiscountPercentage != null ? MathUtil.toFixedDecimal((productDiscountPercentage).doubleValue(), decimals) : null);
            tempProductDto.setIsDeletedElement(isDeletedElement);
            tempProductDto.setCommission(MathUtil.toFixedDecimal(productCommissionAmount.doubleValue(), decimals));
            tempProductDto.setVatCommissionAmount(MathUtil.toFixedDecimal(vatCommissionAmount, decimals));
            tempProductDto.setVatCommissionCode(vatCommissionCodes);
            tempProductDto.setRebate(isRebate);
            tempProductDto.setSurcharge(isSurcharge);
            tempProductDto.setPassengerName(passengerSurname);
        }
//        start = new Date();
        if (queryResult.isEmpty()) {
            //Get the latest information linked to the current version of sailing (that maybe different from the information at booking time)
            Vector<Vector<?>> routeResult = getRouteInfoByEnabledSailing(isBulkAttachment, oaDocument, invoiceID, printInvoiceDto, em);
            //START ISSUE: GRIM-181
            if (routeResult == null || routeResult.isEmpty()) {  //The sailing has been disabled. Get the information linked to the version of sailing at booking time.
                routeResult = getRouteInfoAtBookingTime(isBulkAttachment, oaDocument, invoiceID, printInvoiceDto, em);
            }
            //END ISSUE: GRIM-181
            for (Vector<?> routeRecord : routeResult) {
                tempRouteDto = new InvoiceRouteDto();
                final String legCode = (String) routeRecord.get(0);
                final String routeDepartureDate = (String) routeRecord.get(1);
                final String routeShipName = (String) routeRecord.get(2);
                final String routeArrivalDate = (String) routeRecord.get(3);
                // START JIRA GRIM-822
                final String flexibilityLevel = (String) routeRecord.get(4);
                final String customerReference = (String) routeRecord.get(5);
                // END
                tempRouteDto.setLegCode(retriveLegCodeExtended(legCode, em));
                routes.add((String) legCode);
                tempRouteDto.setDepartureDateAndTime(routeDepartureDate);
                tempRouteDto.setShipName(routeShipName);
                tempRouteDto.setArrivalDateAndTime(routeArrivalDate);
                tempRouteDto.setLineDescription(" ");
                // START JIRA GRIM-822
                tempRouteDto.setFlexibilityLevel(flexibilityLevel);
                // END
//                //START CFL-20
//                tempRouteDto.setArrivalPortDescription(arrivalPortName);
//                tempRouteDto.setDeparturePortDescription(departurePortName);
//                tempRouteDto.setCustomerReference(customerReference);
//                // END
                tempRouteDto.setSubTotal(MathUtil.toFixedDecimal(0D, decimals));
                tempRouteDto.setTotalAmount(MathUtil.toFixedDecimal(0D, decimals));
                tempRouteDto.setTotalCommission(MathUtil.toFixedDecimal(0D, decimals));
                tempRouteDto.setTotalVat(MathUtil.toFixedDecimal(0D, decimals));
                tempRowDto = new InvoiceRowDto();
                tempRowDto.setSubProductList(new ArrayList<>());
                tempProductDto = new InvoiceProductDto();
                tempRowDto.getSubProductList().add(tempProductDto);
                tempRowDto.setProductType(" ");
                tempRouteDto.setInvoiceRowList(new ArrayList<>());
                tempRouteDto.getInvoiceRowList().add(tempRowDto);
                tempRouteDto.setPrintBookingFee(new ArrayList<>());
                tempRouteDto.setPrintBookingSurcharge(new ArrayList<>());
                tempRouteDto.setCustomerReference(customerReference);
                printInvoiceDto.getInvoiceRouteList().add(tempRouteDto);
            }
        }
//        start = new Date();
        // <editor-fold defaultstate="collapsed" desc="Vat statements">
        q = em.createNamedQuery("selectBookingFormKey");
        q.setParameter("bookingreference", bookingReference);
        q.setParameter("bookingStartDate", bookingStartDate);
        q.setParameter("bookingversion", bookingVersion);
        IbeBooking ibeBooking = null;
        try {
            Object result = q.getSingleResult();
            if (result != null) {
                ibeBooking = (IbeBooking) result;
            }
        } catch (Exception e) {
            // Handle the case where no results were found
            ibeBooking = null;
        }
        tempRouteDto.setStatementList(new ArrayList<>());
        financialAdministratorUtils.extractRouteAndVatStatementList(oaDocument,
                null,
                tempRouteDto,
                ibeBooking,
                productTypes,
                decimals,
                divisionName,
                false,
                em);
        // </editor-fold>
//        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> extractRouteAndVatStatementList: " + (DateUtils.getElapsedTime(start, new Date())));
        //BOOKING FEES
//        start = new Date();
        // <editor-fold defaultstate="collapsed" desc="Query Builder">
        sb = new StringBuilder();
        sb.append("SELECT IBF.PRICELISTFEENAME, \n");
        sb.append("       IBF.DESCRIPTION, \n");
        sb.append("       DL.NETAMOUNT, \n");
        sb.append("       DL.COMMISSIONAMOUNT, \n");
        sb.append("       NVL((SELECT SUM(DLFV.VATAMOUNT) \n");
        sb.append("            FROM oa_documentlinefeevat dlfv \n");
        sb.append("            WHERE dlfv.LINENUMBER = dl.LINENUMBER \n");
        sb.append("              AND dlfv.LINESTARTDATE = dl.STARTDATE \n");
        sb.append("              AND dlfv.DOCUMENTID = dl.DOCUMENTID \n");
        sb.append("              AND dlfv.DOCUMENTSTARTDATE = dl.DOCUMENTSTARTDATE \n");
        sb.append("              AND DLFV.VATON = 'FEE'), 0) AS vatamount, \n");
        sb.append("       DL.TOTALAMOUNT, \n");
        sb.append("       (SELECT NVL(LISTAGG(vatname, ', ') WITHIN GROUP (ORDER BY VATNAME), '') \n");
        sb.append("        FROM (SELECT DISTINCT v.DOCUMENTID, v.linenumber, v.VATNAME \n");
        sb.append("              FROM oa_documentlinefeevat v, \n");
        sb.append("                   oa_documentline docline \n");
        sb.append("              WHERE v.LINENUMBER = docline.LINENUMBER \n");
        sb.append("                AND v.LINESTARTDATE = docline.STARTDATE \n");
        sb.append("                AND v.DOCUMENTID = docline.DOCUMENTID \n");
        sb.append("                AND v.DOCUMENTSTARTDATE = docline.DOCUMENTSTARTDATE \n");
        sb.append("                AND v.VATON = 'FEE') vat_query \n");
        sb.append("        WHERE vat_query.documentid = DL.DOCUMENTID \n");
        sb.append("          AND vat_query.linenumber = dl.linenumber) vatcode, \n");
        sb.append("       DLBF.ISBULKAMENDMENT, \n");
        sb.append("       DL.ISDELETEDELEMENT, \n");
        sb.append("       DL.ISCHANGEDPRICE, \n");
        sb.append("       NVL((SELECT SUM(DLFV.VATAMOUNT) \n");
        sb.append("            FROM oa_documentlinefeevat dlfv \n");
        sb.append("            WHERE dlfv.LINENUMBER = dl.LINENUMBER \n");
        sb.append("              AND dlfv.LINESTARTDATE = dl.STARTDATE \n");
        sb.append("              AND dlfv.DOCUMENTID = dl.DOCUMENTID \n");
        sb.append("              AND dlfv.DOCUMENTSTARTDATE = dl.DOCUMENTSTARTDATE \n");
        sb.append("              AND DLFV.VATON = 'COMMISSION'), 0) AS commissionvatamount, \n");
        sb.append("       '' AS commissionvatcode, \n");
        sb.append("       DL.ISPRICEDECREASED, \n");
        sb.append("       (SELECT NVL(LISTAGG(DESCRIPTION, ', ') WITHIN GROUP (ORDER BY DESCRIPTION), '') \n");
        sb.append("        FROM (SELECT DISTINCT v.DOCUMENTID, v.linenumber, ist.DESCRIPTION \n");
        sb.append("              FROM oa_documentlinefeevat v, \n");
        sb.append("                   oa_documentline docline, \n");
        sb.append("                   oa_vatrule vr, \n");
        sb.append("                   oa_invoicestatement ist \n");
        sb.append("              WHERE v.LINENUMBER = docline.LINENUMBER \n");
        sb.append("                AND v.LINESTARTDATE = docline.STARTDATE \n");
        sb.append("                AND v.DOCUMENTID = docline.DOCUMENTID \n");
        sb.append("                AND v.DOCUMENTSTARTDATE = docline.DOCUMENTSTARTDATE \n");
        sb.append("                AND v.VATON = 'FEE' \n");
        sb.append("                AND vr.name = v.VATNAME \n");
        sb.append("                AND vr.STARDATE = v.VATNAMESTARTDATE \n");
        sb.append("                AND ist.VATRULENAME = vr.NAME \n");
        sb.append("                AND ist.VATRULESTARTDATE = vr.STARDATE) vat_query \n");
        sb.append("        WHERE vat_query.documentid = DL.DOCUMENTID \n");
        sb.append("          AND vat_query.linenumber = dl.linenumber) vatdescription, \n");
        sb.append("       (SELECT IBR.DEPARTUREDATE \n");
        sb.append("        FROM IBE_BOOKINGROUTE IBR \n");
        sb.append("        WHERE IBR.BOOKINGREFERENCE = IBF.BOOKINGREFERENCE \n");
        sb.append("          AND IBF.BOOKINGVERSION = IBR.BOOKINGVERSION \n");
        sb.append("          AND IBR.ROUTEINDEX = IBF.ROUTEINDEX) departureDate, \n");
        sb.append("       (SELECT IBR.ROUTEDEPARTUREPORT || '-' || IBR.ROUTEARRIVALPORT \n");
        sb.append("        FROM IBE_BOOKINGROUTE IBR \n");
        sb.append("        WHERE IBR.BOOKINGREFERENCE = IBF.BOOKINGREFERENCE \n");
        sb.append("          AND IBF.BOOKINGVERSION = IBR.BOOKINGVERSION \n");
        sb.append("          AND IBR.ROUTEINDEX = IBF.ROUTEINDEX) portCode, \n");
        sb.append("       vatrates.vatrate, \n");
        sb.append("       CASE \n");
        sb.append("           WHEN vatrates.vatrate IS NULL THEN 0 \n");
        sb.append("           ELSE DL.NETAMOUNT * vatrates.vatrate / 100 \n");
        sb.append("           END AS calculated_vatamount \n");
        sb.append("FROM OA_DOCUMENTLINE DL \n");
        sb.append("INNER JOIN OA_DOCLINEBOOKINGFEE DLBF \n");
        sb.append("ON DLBF.DOCUMENTLINENUMBER = DL.LINENUMBER \n");
        sb.append("   AND DLBF.DOCUMENTLINESTARTDATE = DL.STARTDATE \n");
        sb.append("   AND DLBF.DOCUMENTID = DL.DOCUMENTID \n");
        sb.append("   AND DLBF.DOCUMENTSTARTDATE = DL.DOCUMENTSTARTDATE \n");
        sb.append("INNER JOIN IBE_BOOKINGFEE IBF \n");
        sb.append("ON DLBF.BOOKINGVERSION = IBF.BOOKINGVERSION \n");
        sb.append("   AND DLBF.BOOKINGSTARTDATE = IBF.BOOKINGSTARTDATE \n");
        sb.append("   AND DLBF.PRICELISTFEENAME = IBF.PRICELISTFEENAME \n");
        sb.append("   AND DLBF.PRICELISTFEESTARTDATE = IBF.PRICELISTFEESTARTDATE \n");
        sb.append("   AND DLBF.PRICELISTNAME = IBF.PRICELISTNAME \n");
        sb.append("   AND DLBF.PRICELISTSTARTDATE = IBF.PRICELISTSTARTDATE \n");
        sb.append("   AND DLBF.PRICELISTVERSION = IBF.PRICELISTVERSION \n");
        sb.append("   AND DLBF.BOOKINGFEESTARTDATE = IBF.STARTDATE \n");
        sb.append("   AND DLBF.BOOKINGREFERENCE = IBF.BOOKINGREFERENCE \n");
        sb.append("LEFT JOIN ( \n");
        sb.append("    SELECT DISTINCT v.DOCUMENTID, v.LINENUMBER, v.VATRATE \n");
        sb.append("    FROM oa_documentlinefeevat v \n");
        sb.append("    WHERE v.VATON = 'FEE' \n");
        sb.append(") vatrates \n");
        sb.append("ON DL.DOCUMENTID = vatrates.DOCUMENTID \n");
        sb.append("   AND DL.LINENUMBER = vatrates.LINENUMBER \n");
        sb.append("WHERE DL.DOCUMENTID = ? \n");
        sb.append("  AND DL.ENDDATE = '99991231000000'");
        if (isBulkAttachment) {
            sb.append(" AND DLBF.BOOKINGREFERENCE = ? ");
            sb.append(" AND DLBF.BOOKINGVERSION = ? ");
        }
        q = em.createNativeQuery(sb.toString());
        q.setParameter(1, invoiceID);
        if (isBulkAttachment) {
            q.setParameter(2, ((PrintBulkAttachmentDto) printInvoiceDto).getBookingReference());
            q.setParameter(3, ((PrintBulkAttachmentDto) printInvoiceDto).getBookingVersion());
        }
        queryResult = q.getResultList();
        // <editor-fold defaultstate="collapsed" desc="Booking Fees section creation">
        if (CollectionUtils.isNotEmpty(queryResult)) {
            tempRouteDto.setPrintBookingSurcharge(new ArrayList<>());
            tempRouteDto.getPrintBookingSurcharge().add(tempRowDto = new InvoiceRowDto());
            tempRowDto.setSubProductList(new ArrayList<>());
            tempRowDto.setProductType("Fee"); //TODO: PIERO: internazionalizzare
        }
        for (Object rowObj : queryResult) {
            List<?> row = (List<?>) rowObj;
            final String feeName = (String) row.get(0);
            final String feeDescription = (String) row.get(1);
            final String isBulkAmendmentString = (String) row.get(7);
            isBulkAmendment = StringUtils.getTrueOrFalse(isBulkAmendmentString);
            final String isDeletedElementString = (String) row.get(8);
            final boolean isDeletedFee = StringUtils.getTrueOrFalse(isDeletedElementString);
            final String isChangedPriceString = (String) row.get(9);
            final boolean isChangedPriceFee = StringUtils.getTrueOrFalse(isChangedPriceString);
            final String isPriceDecreasedString = (String) row.get(12);
            final boolean isPriceDecreasedFee = StringUtils.getTrueOrFalse(isPriceDecreasedString);
            final String vatDescriprions = (String) row.get(13); // CFL-2329
            final Date departureDateForFee=(Date) row.get(14);
            final String portCodeForFee=(String) row.get(15);

            //***** netAmount must be:
            //***** POSITIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** NEGATIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            BigDecimal netAmount = (BigDecimal) row.get(2);
            netAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    netAmount,
                    false,
                    isDeletedFee,
                    isChangedPriceFee,
                    isPriceDecreasedFee,
                    false,
                    false);
            //***** commissionAmount must be:
            //***** POSITIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** NEGATIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            BigDecimal commissionAmount = (BigDecimal) row.get(3);
            commissionAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    commissionAmount,
                    true,
                    isDeletedFee,
                    isChangedPriceFee,
                    isPriceDecreasedFee,
                    false,
                    false);
            //***** vatAmount must be:
            //***** POSITIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** NEGATIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            BigDecimal vatAmount = (BigDecimal) row.get(4);
            vatAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    vatAmount,
                    false,
                    isDeletedFee,
                    isChangedPriceFee,
                    isPriceDecreasedFee,
                    false,
                    false);

            BigDecimal vatFeeWithoutRounding= (BigDecimal) row.get(17);
            vatFeeWithoutRounding = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    vatFeeWithoutRounding,
                    false,
                    isDeletedFee,
                    isChangedPriceFee,
                    isPriceDecreasedFee,
                    false,
                    false);
            //***** totalAmount must be:
            //***** POSITIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** NEGATIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            BigDecimal totalAmount = (BigDecimal) row.get(5);
            totalAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    totalAmount,
                    false,
                    isDeletedFee,
                    isChangedPriceFee,
                    isPriceDecreasedFee,
                    false,
                    false);
            String vatCodes = (String) row.get(6);
            // START GRIM-2780
            try {
                if (vatCodes != null && !StringUtils.isEmpty(vatCodes)) {
                    VatEngine vatEngine = new VatEngine();
                    String[] vatNamesSplitted = vatCodes.split(",");
                    vatCodes = "";
                    for (String vatRuleName : vatNamesSplitted) {
                        String vatCode = vatEngine.findVatCodeByVatName(vatRuleName, em);
                        vatCodes = vatCodes.concat(vatCode + ",");
                    }
                    vatCodes = vatCodes.substring(0, vatCodes.length() - 1);
                }
            } catch (EntityValidityException | EntityPersistenceException e) {
                java.util.logging.Logger.getLogger(FinancialAdministrator.class.getName()).log(Level.SEVERE, null, e);
            }
            // END GRIM-2780
            // START GRIM-2084
            if (StringUtils.isEmpty(vatCodes)) {
                UaBusinessunit uaBusinessunit = ibeBooking.getUaBusinessunit();
                UaBusinessunitPK uaBusinessunitPK = uaBusinessunit.getUaBusinessunitPK();
                Enterprise enterprise = DataCaching.getCacheIstance().getEnterprise();
                it.edea.ebooking.business.ua.entity.Company companyselected = EnterpriseUtils.getCompanyByName(enterprise, uaBusinessunitPK.getCompanyname());
                VatEngine vatEngine = GenericFactory.getEBookingInstance(VatEngine.class);
                vatCodes = vatEngine.findOaFeeVatCode(companyselected, em);
            }
            // END GRIM-2084
            //***** commissionAmount must be:
            //***** POSITIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** NEGATIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            BigDecimal vatCommissionAmount = (BigDecimal) row.get(10) != null ? (BigDecimal) row.get(10) : BigDecimal.ZERO;
            vatCommissionAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    vatCommissionAmount,
                    true,
                    isDeletedFee,
                    isChangedPriceFee,
                    isPriceDecreasedFee,
                    false,
                    false);
            tempProductDto = new InvoiceProductDto();
            tempProductDto.setProductCode(feeName);
            tempProductDto.setRouteStr(portCodeForFee);
            tempProductDto.setCurrentDepDate(new SimpleDateFormat("dd/MM/yyyy").format(departureDateForFee));
            tempProductDto.setIsDeletedElement(isDeletedFee);
            setFeeDescription(tempProductDto, feeDescription, divisionName, language);//GRIM-5269: FS-644 - Fees Description Languages Enhancement
            tempProductDto.setAmount(MathUtil.toFixedDecimal((netAmount).doubleValue(), decimals));
            tempProductDto.setAmountWithoutRoundingOff(String.valueOf(netAmount.doubleValue()));
            tempProductDto.setCommission(MathUtil.toFixedDecimal((commissionAmount).doubleValue(), decimals));
            tempProductDto.setVat(MathUtil.toFixedDecimal(MathUtil.sum(vatAmount, vatCommissionAmount).doubleValue(), decimals));
            tempProductDto.setVatWithoutRoundOff(vatFeeWithoutRounding,isCreditNote);
            tempProductDto.setVatDescription(vatDescriprions); //CFL-2329
            tempProductDto.setTotal(MathUtil.toFixedDecimal((totalAmount).doubleValue(), decimals));
            tempProductDto.setVatCode(vatCodes);
            tempProductDto.setRebate(false);
            tempProductDto.setSurcharge(false);
            tempProductDto.setVatCommissionAmount(MathUtil.toFixedDecimal((vatCommissionAmount).doubleValue(), decimals));
            final String vatCommissionCode = (String) row.get(11);
            tempProductDto.setVatCommissionCode(vatCommissionCode);
            if (StringUtils.isNotEmpty(vatCommissionCode) && !commissionsVatCodes.contains(vatCommissionCode)) {
                commissionsVatCodes.add(vatCommissionCode);
            }
            tempRowDto.getSubProductList().add(tempProductDto);
            financialAdministratorUtils.appendFeeStatements(tempProductDto, tempRouteDto, companyName, geographicalLocationName, divisionName, em);
        }
//        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> BOOKING FEES: " + (DateUtils.getElapsedTime(start, new Date())));
        // </editor-fold>
        //BOOKING SURCHARGES
        // <editor-fold defaultstate="collapsed" desc="Query Builder">
        sb = new StringBuilder();
        sb.append("SELECT PS.CODE, ");              //0
        sb.append("       PS.DESCRIPTION, ");       //1
        sb.append("       DL.NETAMOUNT, ");         //2
        sb.append("       DL.COMMISSIONAMOUNT, ");  //3
        sb.append("       0 as VATAMOUNT, ");         //4     //TO-DO: BCFER-87 BCF GAP CTR 006 - Company Tax Regulation
        sb.append("       DL.TOTALAMOUNT, ");       //5
        sb.append("       '' as VATCODE, ");            //6  TO-DO: BCFER-87 BCF GAP CTR 006 - Company Tax Regulation
        sb.append("       DL.ISDELETEDELEMENT, ");            //7
        sb.append("       DL.ISCHANGEDPRICE, ");  //8
        sb.append("       DL.ISPRICEDECREASED ");  //9
        sb.append("FROM OA_DOCUMENTLINE DL, OA_DOCLINEBOOKINGSURCHRG DLBS, PAD_SURCHARGE PS ");
        sb.append("WHERE DL.DOCUMENTID=? AND DL.ENDDATE='99991231000000' AND  DLBS.DOCUMENTLINENUMBER = DL.LINENUMBER   AND ");
        sb.append("     DLBS.DOCUMENTLINESTARTDATE = DL.STARTDATE  AND  DLBS.DOCUMENTID = DL.DOCUMENTID  AND ");
        sb.append("     DLBS.DOCUMENTSTARTDATE = DL.DOCUMENTSTARTDATE AND DLBS.SURCHARGESTARTDATE = PS.STARTDATE AND ");
        sb.append("     DLBS.SURCHARGECODE = PS.CODE AND DLBS.SURCHARGEVERSION = PS.SURCHARGEVERSION ");// </editor-fold>
        if (isBulkAttachment) {
            sb.append(" AND DLBS.BOOKINGREFERENCE = ? ");
            sb.append(" AND DLBS.BOOKINGVERSION = ? ");
        }
        q = em.createNativeQuery(sb.toString());
        q.setParameter(1, invoiceID);
        if (isBulkAttachment) {
            q.setParameter(2, ((PrintBulkAttachmentDto) printInvoiceDto).getBookingReference());
            q.setParameter(3, ((PrintBulkAttachmentDto) printInvoiceDto).getBookingVersion());
        }
        queryResult = q.getResultList();
        // <editor-fold defaultstate="collapsed" desc="Booking Surcharges section creation">
        if (CollectionUtils.isNotEmpty(queryResult)) {
            if (tempRouteDto.getPrintBookingSurcharge() == null) {
                tempRouteDto.setPrintBookingSurcharge(new ArrayList<>());
            }
            tempRouteDto.getPrintBookingSurcharge().add(tempRowDto = new InvoiceRowDto());
            tempRowDto.setSubProductList(new ArrayList<>());
            tempRowDto.setProductType("Surcharge"); //TODO: PIERO: internazionalizzare
        }
        for (Object rowObj : queryResult) {
            List<?> row = (List<?>) rowObj;
            tempProductDto = new InvoiceProductDto();
            final String surchargeCode = (String) row.get(0);
            final String surchargeDescription = (String) row.get(1);
            final String isDeletedElementString = (String) row.get(7);
            final boolean isDeletedSurcharge = StringUtils.getTrueOrFalse(isDeletedElementString);
            final String isChangedPriceString = (String) row.get(8);
            final boolean isChangedPriceSurcharge = StringUtils.getTrueOrFalse(isChangedPriceString);
            final String isPriceDecreasedString = (String) row.get(9);
            final boolean isPriceDecreasedSurcharge = StringUtils.getTrueOrFalse(isPriceDecreasedString);
            BigDecimal netAmount = (BigDecimal) row.get(2);
            netAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    netAmount,
                    false,
                    isDeletedSurcharge,
                    isChangedPriceSurcharge,
                    isPriceDecreasedSurcharge,
                    false,
                    true);
            BigDecimal commissionAmount = (BigDecimal) row.get(3);
            commissionAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    commissionAmount,
                    true,
                    isDeletedSurcharge,
                    isChangedPriceSurcharge,
                    isPriceDecreasedSurcharge,
                    false,
                    true);
            BigDecimal vatAmount = (BigDecimal) row.get(4);
            vatAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    vatAmount,
                    false,
                    isDeletedSurcharge,
                    isChangedPriceSurcharge,
                    isPriceDecreasedSurcharge,
                    false,
                    true);
            BigDecimal totalAmount = (BigDecimal) row.get(5);
            totalAmount = InvoicingUtils.negateIfNeeded(isCreditNote,
                    isBulkAmendment,
                    totalAmount,
                    false,
                    isDeletedSurcharge,
                    isChangedPriceSurcharge,
                    isPriceDecreasedSurcharge,
                    false,
                    true);
            final String bookingSurchargesVatCodes = (String) row.get(6);
            tempProductDto.setProductCode(surchargeCode);
            tempProductDto.setProductDesc(surchargeDescription);
            tempProductDto.setAmount(MathUtil.toFixedDecimal((netAmount).doubleValue(), decimals));
            tempProductDto.setAmountWithoutRoundingOff(String.valueOf(netAmount.doubleValue()));
            tempProductDto.setCommission(MathUtil.toFixedDecimal((commissionAmount).doubleValue(), decimals));
            tempProductDto.setVat(MathUtil.toFixedDecimal((vatAmount).doubleValue(), decimals));
            tempProductDto.setTotal(MathUtil.toFixedDecimal((totalAmount).doubleValue(), decimals));
            tempProductDto.setVatCode(bookingSurchargesVatCodes);
            tempProductDto.setIsDeletedElement(isDeletedSurcharge);
            tempProductDto.setRebate(false);
            tempProductDto.setSurcharge(true);
            tempRowDto.getSubProductList().add(tempProductDto);
        }
//        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> BOOKING SURCHARGES: " + (DateUtils.getElapsedTime(start, new Date())));
        // </editor-fold>
        //***** Calculate again the totals to avoid rounding problem with incorrect data stored on the database **************************************
//        start = new Date();
        Collection<InvoiceRouteDto> invoiceRouteList = printInvoiceDto.getInvoiceRouteList();
        BigDecimal totalNetCommission = BigDecimal.ZERO;
        BigDecimal totalInvoiceVatCommission = BigDecimal.ZERO;
        BigDecimal bookingSubTotal = BigDecimal.ZERO;
        BigDecimal bookingVatTotal = BigDecimal.ZERO;
        BigDecimal bookingTotal = BigDecimal.ZERO;
        // START GRIM-2747
        HashMap<String, String> maplegCodes = new HashMap<>();
        for (InvoiceRouteDto invoiceRouteDto : invoiceRouteList) {
            String legCode = invoiceRouteDto.getLegCode();
            maplegCodes.put(legCode, legCode);
            // END GRIM-2747
            Collection<InvoiceRowDto> invoiceRowList = invoiceRouteDto.getInvoiceRowList();
            BigDecimal totalRouteAmount = BigDecimal.ZERO;
            BigDecimal totalRouteVat = BigDecimal.ZERO;
            BigDecimal totalRouteVatCommission = BigDecimal.ZERO;
            BigDecimal subTotalRoute = BigDecimal.ZERO;
            BigDecimal totalRouteCommission = BigDecimal.ZERO;
            for (InvoiceRowDto invoiceRowDto : invoiceRowList) {
                Collection<InvoiceProductDto> subProductList = invoiceRowDto.getSubProductList();
                for (InvoiceProductDto invoiceProductDto : subProductList) {
                    final String vatCommissionCodes = invoiceProductDto.getVatCommissionCode();
                    if (vatCommissionCodes != null) {
                        String[] values = vatCommissionCodes.split(", ");
                        commissionsVatCodes.addAll(Arrays.asList(values));
                    }
                    final String productQuantity = invoiceProductDto.getProductQuantity();
                    final String productUnitPrice = invoiceProductDto.getUnitPrice();
                    final String productDiscountAmount = invoiceProductDto.getDiscount();
                    final String productCommissionsString = invoiceProductDto.getCommission();
                    BigDecimal productCommissions = MathUtil.getPreciseBigDecimal(productCommissionsString);
                    final String productCommissionVatString = invoiceProductDto.getVatCommissionAmount();
                    BigDecimal productCommissionVat = MathUtil.getPreciseBigDecimal(productCommissionVatString);
                    final String productVat = invoiceProductDto.getVat();
                    final String productVatWithoutRoundOff =Optional.ofNullable(invoiceProductDto.getVatWithoutRoundOff())
                            .map(Object::toString)
                            .orElse(null);
                    final BigDecimal productSubTotal = MathUtil.subtract(MathUtil.multiply(productQuantity, productUnitPrice), productDiscountAmount);
                    invoiceProductDto.setAmount(MathUtil.toFixedDecimal(productSubTotal.toString(), decimals));
                    invoiceProductDto.setAmountWithoutRoundingOff(String.valueOf(productSubTotal.doubleValue()));
                    final BigDecimal productTotal = MathUtil.sum(MathUtil.subtract(MathUtil.multiply(productQuantity, productUnitPrice),
                                    productDiscountAmount),
                            productVatWithoutRoundOff);
                    invoiceProductDto.setTotal(MathUtil.toFixedDecimal(productTotal.toString(), decimals));
                    bookingSubTotal = MathUtil.sum(bookingSubTotal, productSubTotal);
                    bookingVatTotal = MathUtil.sum(bookingVatTotal, productVatWithoutRoundOff);
                    bookingTotal = MathUtil.sum(bookingTotal, productTotal);
                    subTotalRoute = MathUtil.sum(subTotalRoute, productSubTotal);
                    //***** productCommissions must be:
                    //***** NEGATIVE for:
                    //                      - INVOICE
                    //                      - CREDIT NOTE and ISBULKAMENDMENT
                    //***** POSITIVE for:
                    //                      - CREDIT NOTE
                    //                      - INVOICE and ISBULKAMENDMENT
                    productCommissions = InvoicingUtils.negateIfNeeded(isCreditNote,
                            isBulkAmendment,
                            productCommissions,
                            true,
                            isDeletedElement,
                            isChangedPrice,
                            isPriceDecreased,
                            invoiceProductDto.isRebate(),
                            invoiceProductDto.isSurcharge());
                    //***** productCommissions must be:
                    //***** NEGATIVE for:
                    //                      - INVOICE
                    //                      - CREDIT NOTE and ISBULKAMENDMENT
                    //***** POSITIVE for:
                    //                      - CREDIT NOTE
                    //                      - INVOICE and ISBULKAMENDMENT
                    productCommissionVat = InvoicingUtils.negateIfNeeded(isCreditNote,
                            isBulkAmendment,
                            productCommissionVat,
                            true,
                            isDeletedElement,
                            isChangedPrice,
                            isPriceDecreased,
                            invoiceProductDto.isRebate(),
                            invoiceProductDto.isSurcharge());
                    totalRouteCommission = MathUtil.sum(totalRouteCommission, productCommissions);
                    totalRouteVat = MathUtil.sum(totalRouteVat, productVatWithoutRoundOff);
                    totalRouteVatCommission = MathUtil.sum(totalRouteVatCommission, productCommissionVat);
                    totalRouteAmount = MathUtil.sum(totalRouteAmount, productSubTotal,productVatWithoutRoundOff);
                }
            }
            Collection<PrintRowDto> printBookingSurcharge = invoiceRouteDto.getPrintBookingSurcharge();
            for (PrintRowDto printRowDto : printBookingSurcharge) {
                Collection<InvoiceProductDto> subProductList = ((InvoiceRowDto) printRowDto).getSubProductList();
                for (InvoiceProductDto invoiceProductDto : subProductList) {
                    bookingSubTotal = MathUtil.sum(bookingSubTotal, invoiceProductDto.getAmountWithoutRoundingOff());
                    if(invoiceProductDto.getVatWithoutRoundOff()==null){
                        //THIS IS A FEE (SO TAKE THE FEE TOTAL)
                        invoiceProductDto.setVatWithoutRoundOff(new BigDecimal(invoiceProductDto.getVat()),isCreditNote);
                    }
                    bookingVatTotal = MathUtil.sum(bookingVatTotal, invoiceProductDto.getVatWithoutRoundOff().toString());
                    BigDecimal currentTotal = MathUtil.sum(invoiceProductDto.getAmountWithoutRoundingOff(), invoiceProductDto.getVatWithoutRoundOff().toString());
                    bookingTotal = MathUtil.sum(bookingTotal, currentTotal);
                    BigDecimal commission = StringUtils.isNotEmpty(invoiceProductDto.getCommission())
                            ? MathUtil.getPreciseBigDecimal(invoiceProductDto.getCommission())
                            : BigDecimal.ZERO;
                    commission = InvoicingUtils.negateIfNeeded(isCreditNote,
                            isBulkAmendment,
                            commission,
                            true,
                            invoiceProductDto.isIsDeletedElement(),
                            isChangedPrice,
                            isPriceDecreased,
                            invoiceProductDto.isRebate(),
                            invoiceProductDto.isSurcharge());
                    totalNetCommission = MathUtil.sum(totalNetCommission, commission);
                    BigDecimal vatCommission = StringUtils.isNotEmpty(invoiceProductDto.getVatCommissionAmount())
                            ? MathUtil.getPreciseBigDecimal(invoiceProductDto.getVatCommissionAmount())
                            : BigDecimal.ZERO;
                    totalInvoiceVatCommission = MathUtil.sum(totalInvoiceVatCommission, vatCommission);
                }
            }
            //***** subTotalRoute must be:
            //***** POSITIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** NEGATIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            invoiceRouteDto.setSubTotal(MathUtil.toFixedDecimal(subTotalRoute, decimals));
            //***** totalRouteAmount must be:
            //***** POSITIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** NEGATIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            invoiceRouteDto.setTotalAmount(MathUtil.toFixedDecimal(totalRouteAmount, decimals));
            //***** totalRouteCommission must be:
            //***** NEGATIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** POSITIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            invoiceRouteDto.setTotalCommission(MathUtil.toFixedDecimal(totalRouteCommission, decimals));
            //***** totalRouteVatCommission must be:
            //***** NEGATIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** POSITIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            invoiceRouteDto.setTotalVatCommission(MathUtil.toFixedDecimal(totalRouteVatCommission, decimals));
            //***** totalRouteVat must be:
            //***** POSITIVE for:
            //                      - INVOICE
            //                      - CREDIT NOTE and ISBULKAMENDMENT
            //***** NEGATIVE for:
            //                      - CREDIT NOTE
            //                      - INVOICE and ISBULKAMENDMENT
            invoiceRouteDto.setTotalVat(MathUtil.toFixedDecimal(totalRouteVat, decimals));
            totalNetCommission = MathUtil.sum(totalNetCommission, totalRouteCommission);
            totalInvoiceVatCommission = MathUtil.sum(totalInvoiceVatCommission, totalRouteVatCommission);
        }
//        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> Calculate totals loop: " + (DateUtils.getElapsedTime(start, new Date())));
        //********************************************************************************************************************************************
        StringBuilder commissionVatCode = new StringBuilder();
        int c = 1;
        for (String commissionVatCodeItem : commissionsVatCodes) {
            commissionVatCode.append(commissionVatCodeItem);
            if (c < commissionsVatCodes.size()) {
                commissionVatCode.append(", ");
                c++;
            }
        }
        hm.put("invoiceCommission", MathUtil.toFixedDecimal(totalNetCommission, decimals));
        hm.put("commissionVatCode", commissionVatCode.toString());
        hm.put("commissionTotalVat", MathUtil.toFixedDecimal(totalInvoiceVatCommission, decimals));
        final String totalInvoiceCommission = MathUtil.toFixedDecimal(MathUtil.sum(MathUtil.toFixedDecimal(totalNetCommission, decimals),
                MathUtil.toFixedDecimal(totalInvoiceVatCommission, decimals)
        ), decimals);
        hm.put("invoiceTotalCommission", totalInvoiceCommission);
//        start = new Date();
        String invoiceNotes = financialAdministratorUtils.generatesInvoiceNotes(divisionName, language, currencyName, companyName, maplegCodes); // GRIM-2747
        String manualNotes = oaDocument.getInvoicenotes();
        if (StringUtils.isNotEmpty(manualNotes)) {
            invoiceNotes = invoiceNotes.concat(manualNotes);
        }
        hm.put("invoiceNotes", invoiceNotes);
//        logger.fine("********************* PERFORMANCE TEST generateSingleInvoice ---> generatesInvoiceNotes: " + (DateUtils.getElapsedTime(start, new Date())));
        //***** bookingSubTotal must be:
        //***** POSITIVE for:
        //                      - INVOICE
        //                      - CREDIT NOTE and ISBULKAMENDMENT
        //***** NEGATIVE for:
        //                      - CREDIT NOTE
        //                      - INVOICE and ISBULKAMENDMENT
        hm.put("bookingNetAmount", MathUtil.toFixedDecimal(bookingSubTotal, decimals));
        hm.put("bookingNetAmountBD", new BigDecimal((String)hm.get("bookingNetAmount")));
        //***** bookingVatTotal must be:
        //***** POSITIVE for:
        //                      - INVOICE
        //                      - CREDIT NOTE and ISBULKAMENDMENT
        //***** NEGATIVE for:
        //                      - CREDIT NOTE
        //                      - INVOICE and ISBULKAMENDMENT
        if(isBulkAttachment) {
            hm.put("bookingVatAmount", bookingVatTotal.toString());
            hm.put("bookingVatAmountBD",new BigDecimal(MathUtil.toFixedDecimal(new BigDecimal((String)hm.get("bookingVatAmount")),decimals)) );
        }else {
            hm.put("bookingVatAmount", MathUtil.toFixedDecimal(bookingVatTotal, decimals));
            hm.put("bookingVatAmountBD", new BigDecimal((String)hm.get("bookingVatAmount")));
        }
        //***** bookingTotal must be:
        //***** POSITIVE for:
        //                      - INVOICE
        //                      - CREDIT NOTE and ISBULKAMENDMENT
        //***** NEGATIVE for:
        //                      - CREDIT NOTE
        //                      - INVOICE and ISBULKAMENDMENT
        hm.put("bookingTotalAmount", MathUtil.toFixedDecimal(bookingTotal, decimals));
        hm.put("bookingTotalAmountBD",  new BigDecimal((String)hm.get("bookingTotalAmount")));
        //***** invoiceTotal must be:
        //***** POSITIVE for:
        //                      - INVOICE
        //                      - CREDIT NOTE and ISBULKAMENDMENT
        //***** NEGATIVE for:
        //                      - CREDIT NOTE
        //                      - INVOICE and ISBULKAMENDMENT
        BigDecimal invoiceTotal = MathUtil.sum(bookingTotal, totalInvoiceCommission);
        hm.put("invoiceTotalAmount", MathUtil.toFixedDecimal(invoiceTotal, decimals));
        //***** amountPaid must be:
        //***** POSITIVE for:
        //                      - INVOICE
        //                      - CREDIT NOTE and ISBULKAMENDMENT
        //***** NEGATIVE for:
        //                      - CREDIT NOTE
        //                      - INVOICE and ISBULKAMENDMENT
        //START GRIM-5273 | FS-605
        BigDecimal invoiceBalance = MathUtil.subtract(invoiceTotal, amountPaid);
        boolean isReceipt = false;
        if (!isCreditNote) {
            String title = financialAdministratorUtils.getDocumentTitle(divisionName, invoiceBalance, language);
            if (StringUtils.isNotEmpty(title)) {
                hm.put("title", title);
            }
            if (invoiceBalance.intValue() == 0) {
                isReceipt = true;
            }
        }
        hm.put("isReceipt", isReceipt);
        //END GRIM-5273 | FS-605
        if (financialAdministratorUtils.printInvoiceBalance(divisionName) && !checkCreditCustomer(accAccount.getAccounttype().getName())) {
            hm.put("printInvoiceBalance", Boolean.TRUE);
            hm.put("invoiceBalance", MathUtil.toFixedDecimal(invoiceBalance, decimals));
        } else {
            hm.put("printInvoiceBalance", Boolean.FALSE);
        }
        //PRINT COMMISSION PARAMETER
        Boolean printCommission = financialAdministratorUtils.checkPrintCommission(uaCompany, accFinancialdetail, totalInvoiceCommission);
        hm.put("printCommission", printCommission);
        if (printCommission) {
            financialAdministratorUtils.appendCustomStatements(tempRouteDto, uaCompany.getUaCompanyPK().getName(), em);
        }
        // START CFL-20
        if(accAccount.getAccountTypeName() != null) {
            hm.put("accountType", accAccount.getAccountTypeName());
        }
        if (!invoiceToBookingholder) {
            hm.put("accountJdeNumber", accFinancialdetail.getJdeaddressbooknumber());
        }

        if(isCreditNote && oaDocument.getOaConnectedDocument() != null) {
            String connectedDocumentNumber = oaDocument.getOaConnectedDocument().getDocumentnumber();
            hm.put("connectedDocumentNumber",  connectedDocumentNumber);
        }

        BigDecimal transAmountPaid = isCreditNote ? invoiceTotal.negate() : retrieveTotalBookingTransactionPayment(em, bookingReference);
        if(transAmountPaid != null) {
            hm.put("transAmountPaid",  MathUtil.toFixedDecimal(transAmountPaid, decimals));
            hm.put("transAmountPaidBD",  new BigDecimal((String) hm.get("transAmountPaid")));
            BigDecimal transInvoiceBalance = MathUtil.subtract(invoiceTotal, transAmountPaid);
            hm.put("transInvoiceBalance",  MathUtil.toFixedDecimal(transInvoiceBalance, decimals));
            hm.put("transInvoiceBalanceBD",  new BigDecimal((String) hm.get("transInvoiceBalance")));
        }
        hm.put("creditNoteBalance",  MathUtil.toFixedDecimal(BigDecimal.ZERO, decimals));
        hm.put("creditNoteBalanceBD",   new BigDecimal((String) hm.get("creditNoteBalance")));
        hm.put("isRefundPayment", isCreditNote || (transAmountPaid != null && transAmountPaid.signum() < 0));


        // END CFL-20
        printInvoiceDto.setHmParameter(hm);
        if (isBulkAttachment) {
            ((PrintBulkAttachmentDto) printInvoiceDto).setTicketNumber(ticketNumber);
            ((PrintBulkAttachmentDto) printInvoiceDto).setDate(formatter.format(documentDate));
        }

        //START CFL-20
        extractSingleInvoiceDenormalizedRows(hm, printInvoiceDto, bookingReference, ticketNumber, bookingHolderName, bookingHolderSurname, bookingDate, decimals);
        // END
    }
