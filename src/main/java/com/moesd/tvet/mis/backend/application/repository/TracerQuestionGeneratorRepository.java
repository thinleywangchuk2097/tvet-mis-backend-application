package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.TracerQuestionGenerator;
import jakarta.persistence.Tuple;

public interface TracerQuestionGeneratorRepository extends JpaRepository<TracerQuestionGenerator, Long>{
	
	@Query(value =  
			"SELECT "
					+ "  tq.id, "
					+ "  tq.application_no, "
					+ "  tq.tracer_title, "
					+ "  tq.parent_tracer_type_id, "
					+ "  tq.sub_tracer_type_id, "
					+ "  tq.question_type_id, "
					+ "  tq.question_text, "
					+ "  tq.is_required, "
					+ "  tq.question_order, "
					+ "  tq.rating_scale, "
					+ "  tq.created_at, "
					+ "  tq.updated_at, "
					+ "  (SELECT "
					+ "    IFNULL( "
					+ "      JSON_ARRAYAGG( "
					+ "        JSON_OBJECT( "
					+ "          'id', opt.id, "
					+ "          'optionText', opt.option_text, "
					+ "          'optionOrder', opt.option_order "
					+ "        ) "
					+ "      ), "
					+ "      JSON_ARRAY() "
					+ "    ) "
					+ "  FROM tbl_tracer_question_option_dtls opt "
					+ "  WHERE opt.question_id = tq.id) AS question_options, "
					+ "  (SELECT "
					+ "    IFNULL( "
					+ "      JSON_ARRAYAGG( "
					+ "        JSON_OBJECT( "
					+ "          'id', sub.id, "
					+ "          'questionText', sub.question_text, "
					+ "          'questionTypeId', sub.question_type_id, "
					+ "          'isRequired', sub.is_required, "
					+ "          'subQuestionOrder', sub.sub_question_order, "
					+ "          'ratingScale', sub.rating_scale, "
					+ "          'createdAt', sub.created_at, "
					+ "          'updatedAt', sub.updated_at, "
					+ "          'options', ( "
					+ "            SELECT "
					+ "              IFNULL( "
					+ "                JSON_ARRAYAGG( "
					+ "                  JSON_OBJECT( "
					+ "                    'id', sub_opt.id, "
					+ "                    'optionText', sub_opt.option_text, "
					+ "                    'optionOrder', sub_opt.option_order "
					+ "                  ) "
					+ "                ), "
					+ "                JSON_ARRAY() "
					+ "              ) "
					+ "            FROM tbl_tracer_sub_question_option_dtls sub_opt "
					+ "            WHERE sub_opt.sub_question_id = sub.id "
					+ "          ) "
					+ "        ) "
					+ "      ), "
					+ "      JSON_ARRAY() "
					+ "    ) "
					+ "  FROM tbl_tracer_sub_question_dtls sub "
					+ "  WHERE sub.question_id = tq.id) AS sub_questions "
					+ " "
					+ "FROM tbl_tracer_question_dtls tq "
					+ "WHERE tq.application_no = ? "
					+ "ORDER BY tq.question_order ASC", nativeQuery = true)
		List<Tuple> getTracerDetailsByApplicationNo(String application_no);
	
		@Query(value =  
				"SELECT "
					+ "  a.application_no, "
					+ "  a.created_at, "
					+ "  a.tracer_title, "
					+ "  b.dropdown_name AS parent_tracer_type, "
					+ "  c.name AS sub_tracer_type, "
					+ "  a.* "
					+ "FROM "
					+ "  tbl_tracer_question_dtls a "
					+ "  LEFT JOIN tbl_dropdown_master b "
					+ "    ON a.parent_tracer_type_id = b.id "
					+ "  LEFT JOIN tbl_dropdown_child_master c "
					+ "    ON c.id = a.sub_tracer_type_id "
					+ "    GROUP BY a.application_no", nativeQuery = true)
		List<Tuple> getTracerAllApplications();
	

}
