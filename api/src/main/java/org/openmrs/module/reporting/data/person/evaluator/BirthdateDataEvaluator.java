/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.reporting.data.person.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.Birthdate;
import org.openmrs.module.reporting.data.person.EvaluatedPersonData;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Evaluates a BirthdateDataDefinition to produce a PersonData
 */
@Handler(supports=BirthdateDataDefinition.class, order=50)
public class BirthdateDataEvaluator implements PersonDataEvaluator {

	static int PARAMETER_LIMIT = 50;
	@Autowired
	EvaluationService evaluationService;

	/** 
	 * @see PersonDataEvaluator#evaluate(PersonDataDefinition, EvaluationContext)
	 * @should return all birth dates for all persons
	 */
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context) throws EvaluationException {
		EvaluatedPersonData c = new EvaluatedPersonData(definition, context);

		/**
		 * Switched from hql to sql since hql cannot handle a the list param
		 * TODO: investigate the actual cause
		 */
		String sql = "select p.person_id, p.birthdate, p.birthdate_estimated from person p where p.voided=0 and p.person_id in (:personIds)";
		SqlQueryBuilder qb = new SqlQueryBuilder();
		qb.append(sql);
		qb.addParameter("personIds", context.getBaseCohort());

		List<Object[]> results = evaluationService.evaluateToList(qb, context);

		List<Integer> ret = new ArrayList<Integer>();
		for (Integer pId : context.getBaseCohort().getMemberIds()) {
			ret.add(pId);
		}

		for (Object[] row : results) {
			Integer pId = (Integer)row[0];
			Date birthdate = (Date)row[1];
			boolean estimated = (row[2] == Boolean.TRUE);
			if (birthdate != null) {
				c.addData(pId, new Birthdate(birthdate, estimated));
			}
			else {
				c.addData(pId, null);
			}
		}


		return c;
	}

}
