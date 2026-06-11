package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_tracer_question_dtls")
public class TracerQuestionGenerator {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_no", nullable = false)
    private String applicationNo;
    
    @Column(name = "tracer_title", nullable = false)
    private String tracerTitle;
    
    private String parentTracerTypeId;
    
    private String subTracerTypeId;
    
    @Column(name = "question_type_id")
    private Integer questionTypeId;
    
    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;
    
    @Column(name = "is_required")
    private Integer isRequired;
    
    @Column(name = "question_order")
    private Integer questionOrder;
    
    @Column(name = "rating_scale")
    private Integer ratingScale;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Builder.Default
    @OneToMany(mappedBy = "tracerQuestionGenerator", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TracerSubQuestionGenerator> tracerSubQuestionGenerator = new ArrayList<>();
    
    @Builder.Default
    @OneToMany(mappedBy = "tracerQuestionGenerator", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TracerQuestionGeneratorOptionId> options = new ArrayList<>();
    
    // Helper methods
    public void addSubQuestion(TracerSubQuestionGenerator subQuestion) {
        tracerSubQuestionGenerator.add(subQuestion);
        subQuestion.setTracerQuestionGenerator(this);
    }
    
    public void addOption(TracerQuestionGeneratorOptionId option) {
        options.add(option);
        option.setTracerQuestionGenerator(this);
    }
}