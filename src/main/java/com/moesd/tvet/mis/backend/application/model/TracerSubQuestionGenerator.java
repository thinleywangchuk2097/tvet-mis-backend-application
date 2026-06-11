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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "tbl_tracer_sub_question_dtls")
public class TracerSubQuestionGenerator {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;
    
    @Column(name = "question_type_id", nullable = false)
    private Integer questionTypeId;
    
    @Column(name = "is_required")
    private Integer isRequired;
    
    @Column(name = "sub_question_order")
    private Integer subQuestionOrder;
    
    @Column(name = "rating_scale")
    private Integer ratingScale;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne
    @JoinColumn(name = "question_id", referencedColumnName = "id")
    private TracerQuestionGenerator tracerQuestionGenerator;
    
    @Builder.Default
    @OneToMany(mappedBy = "tracerSubQuestionGenerator", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TracerSubQuestionGeneratorOptionId> options = new ArrayList<>();
    
    // Helper methods
    public void addOption(TracerSubQuestionGeneratorOptionId option) {
        options.add(option);
        option.setTracerSubQuestionGenerator(this);
    }
}