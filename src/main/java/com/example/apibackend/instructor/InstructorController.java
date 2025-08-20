package com.example.apibackend.instructor;

import com.example.apibackend.course.CourseRepository;
import com.example.apibackend.course.CourseSummaryDto;
import com.example.apibackend.review.ReviewRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/instructors")
public class InstructorController {
    private final InstructorRepository instructorRepo;
    private final CourseRepository courseRepo;
    private final ReviewRepository reviewRepo;

    public InstructorController(InstructorRepository instructorRepo, CourseRepository courseRepo, ReviewRepository reviewRepo) {
        this.instructorRepo = instructorRepo;
        this.courseRepo = courseRepo;
        this.reviewRepo = reviewRepo;
    }

    @GetMapping
    public List<InstructorSummaryDto> getAllInstructors() {
        return instructorRepo.findAll().stream().map(InstructorSummaryDto::fromEntity).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstructorDetailDto> getInstructor(@PathVariable Long id) {
        return instructorRepo.findById(id)
            .map(instructor -> {
                // You may need to implement findByInstructorId in CourseRepository
                List<CourseSummaryDto> courses = courseRepo.findByInstructorId(instructor.getId())
                    .stream()
                    .map(c -> new CourseSummaryDto(
                        c.getId(),
                        c.getTitle(),
                        c.getSlug(),
                        c.getShortDescription(),
                        c.getPriceCents(),
                        c.getLevel(),
                        c.getIsActive(),
                        reviewRepo.findAverageRatingByCourseId(c.getId()) != null ? reviewRepo.findAverageRatingByCourseId(c.getId()) : 0.0,
                        InstructorSummaryDto.fromEntity(c.getInstructor())
                    ))
                    .collect(Collectors.toList());
                return InstructorDetailDto.fromEntity(instructor, courses);
            })
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    public static class InstructorSummaryDto {
        public Long id;
        public String name;
        public String bio;
        public String avatarUrl;
        public static InstructorSummaryDto fromEntity(Instructor i) {
            InstructorSummaryDto dto = new InstructorSummaryDto();
            dto.id = i.getId();
            dto.name = i.getName();
            dto.bio = i.getBio();
            dto.avatarUrl = i.getAvatarUrl();
            return dto;
        }
    }

    public static class InstructorDetailDto extends InstructorSummaryDto {
        public List<CourseSummaryDto> courses;
        public static InstructorDetailDto fromEntity(Instructor i, List<CourseSummaryDto> courses) {
            InstructorDetailDto dto = new InstructorDetailDto();
            dto.id = i.getId();
            dto.name = i.getName();
            dto.bio = i.getBio();
            dto.avatarUrl = i.getAvatarUrl();
            dto.courses = courses;
            return dto;
        }
    }
}
