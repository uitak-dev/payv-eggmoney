package com.eggmoney.payv.presentation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eggmoney.payv.application.service.CategoryAppService;
import com.eggmoney.payv.domain.model.entity.Category;
import com.eggmoney.payv.domain.model.vo.CategoryId;
import com.eggmoney.payv.domain.model.vo.LedgerId;
import com.eggmoney.payv.domain.shared.error.DomainException;
import com.eggmoney.payv.presentation.dto.CategoryCreateDto;
import com.eggmoney.payv.presentation.dto.CategoryNodeDto;
import com.eggmoney.payv.presentation.dto.CategoryUpdateDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 카테고리 컨트롤러
 * @author 정의탁
 */
@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/ledgers/{ledgerId}/categories")
public class CategoryController {

	private final CategoryAppService categoryAppService;

    // ===== 목록 페이지 =====
    @GetMapping
    public String list(@PathVariable String ledgerId,
                       @ModelAttribute("message") String message,
                       @ModelAttribute("error") String error,
                       Model model) {
    	
        LedgerId lId = LedgerId.of(ledgerId);
        List<Category> all = categoryAppService.listByLedger(lId);
        List<CategoryNodeDto> roots = toTree(all);

        model.addAttribute("ledgerId", ledgerId);
        model.addAttribute("roots", roots); // 루트 + 자식(최대 2-depth)
        model.addAttribute("currentPage", "categories"); // 현재 페이지 정보를 모델에 전달(aside에 호버된 상태 표시하기 위함)
        return "categories/list";
    }

    // ===== 신규 폼 =====
    @GetMapping("/new")
    public String newForm(@PathVariable String ledgerId, Model model) {
        LedgerId lId = LedgerId.of(ledgerId);
        // 부모 선택은 "루트만" 노출.(2-depth 보장)
        List<Category> roots = categoryAppService.listByLedger(lId).stream()
                .filter(c -> c.getParentId() == null)
                .sorted(Comparator.comparingInt(Category::getSortOrder).thenComparing(Category::getName))
                .collect(Collectors.toList());

        model.addAttribute("ledgerId", ledgerId);
        model.addAttribute("roots", roots);
        model.addAttribute("form", new CategoryCreateDto());
        model.addAttribute("currentPage", "categories"); // 현재 페이지 정보를 모델에 전달(aside에 호버된 상태 표시하기 위함)
        return "categories/new";
    }

    // ===== 신규 처리 =====
    @PostMapping
    public String create(@PathVariable String ledgerId,
                         @ModelAttribute("form") CategoryCreateDto form,
                         RedirectAttributes ra) {
        try {
            if (isBlank(form.getName())) {
                ra.addFlashAttribute("error", "이름은 필수입니다.");
                return "redirect:/ledgers/" + ledgerId + "/categories/new";
            }
            LedgerId lId = LedgerId.of(ledgerId);
            int nextOrder = nextSortOrder(lId, form.getParentId());

            if (isBlank(form.getParentId())) {
                categoryAppService.createRoot(lId, form.getName().trim(), false, nextOrder);
            } else {
                categoryAppService.createChild(lId, CategoryId.of(form.getParentId()), form.getName().trim(), false, nextOrder);
            }
            ra.addFlashAttribute("message", "카테고리를 생성했습니다.");
            return "redirect:/ledgers/" + ledgerId + "/categories";
        } 
        catch (DomainException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ledgers/" + ledgerId + "/categories/new";
        }
    }

    // ===== 수정 폼 =====
    @GetMapping("/{categoryId}/edit")
    public String editForm(@PathVariable String ledgerId,
                           @PathVariable String categoryId,
                           RedirectAttributes ra,
                           Model model) {
    	
        Category c = categoryAppService.getDetails(CategoryId.of(categoryId));
        if (c.isSystemCategory()) {
            ra.addFlashAttribute("error", "시스템 카테고리는 수정할 수 없습니다.");
            return "redirect:/ledgers/" + ledgerId + "/categories";
        }
        
        model.addAttribute("ledgerId", ledgerId);
        model.addAttribute("category", c);
        model.addAttribute("form", new CategoryUpdateDto(c.getName()));
        model.addAttribute("currentPage", "categories"); // 현재 페이지 정보를 모델에 전달(aside에 호버된 상태 표시하기 위함)
        return "categories/edit";
    }

    // ===== 수정 처리 =====
    @PostMapping("/{categoryId}")
    public String update(@PathVariable String ledgerId,
                         @PathVariable String categoryId,
                         @ModelAttribute("form") CategoryUpdateDto form,
                         RedirectAttributes ra) {
        try {
            if (isBlank(form.getName())) {
                ra.addFlashAttribute("error", "이름은 필수입니다.");
                return "redirect:/ledgers/" + ledgerId + "/categories/" + categoryId + "/edit";
            }
            categoryAppService.rename(CategoryId.of(categoryId), LedgerId.of(ledgerId), form.getName().trim());
            ra.addFlashAttribute("message", "카테고리를 수정했습니다.");
            return "redirect:/ledgers/" + ledgerId + "/categories";
        } 
        catch (DomainException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ledgers/" + ledgerId + "/categories/" + categoryId + "/edit";
        }
    }

    // ===== 삭제 (AJAX) =====
    @DeleteMapping(value = "/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> deleteAjax(@PathVariable String ledgerId, @PathVariable String categoryId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Category c = categoryAppService.getDetails(CategoryId.of(categoryId));
            if (c.isSystemCategory()) {
                res.put("ok", false); 
                res.put("message", "시스템 카테고리는 삭제할 수 없습니다.");
                return res;
            }
            categoryAppService.delete(LedgerId.of(ledgerId), CategoryId.of(categoryId));
            res.put("ok", true);
        } 
        catch (DomainException e) {
            res.put("ok", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

	// ===== 내부 유틸 =====
	private List<CategoryNodeDto> toTree(List<Category> all) {
		Map<String, CategoryNodeDto> map = new LinkedHashMap<>();
		for (Category c : all) {
			CategoryNodeDto n = new CategoryNodeDto();
			n.setId(c.getId().toString());
			n.setName(c.getName());
			n.setParentId(c.getParentId() == null ? null : c.getParentId().toString());
			n.setSystem(c.isSystemCategory());
			n.setSortOrder(c.getSortOrder());
			map.put(n.getId(), n);
		}
		List<CategoryNodeDto> roots = new ArrayList<>();
		for (Category c : all) {
			CategoryNodeDto n = map.get(c.getId().toString());
			if (c.getParentId() == null)
				roots.add(n);
			else {
				CategoryNodeDto p = map.get(c.getParentId().toString());
				if (p != null)
					p.getChildren().add(n);
				else
					roots.add(n);
			}
		}
		Comparator<CategoryNodeDto> cmp = Comparator.comparingInt(CategoryNodeDto::getSortOrder)
				.thenComparing(CategoryNodeDto::getName, String.CASE_INSENSITIVE_ORDER);
		roots.sort(cmp);
		roots.forEach(r -> r.getChildren().sort(cmp));
		return roots;
	}

	private int nextSortOrder(LedgerId ledgerId, String parentIdOrNull) {
		List<Category> all = categoryAppService.listByLedger(ledgerId);
		return all.stream().filter(c -> {
			String pid = c.getParentId() == null ? null : c.getParentId().toString();
			return Objects.equals(pid, isBlank(parentIdOrNull) ? null : parentIdOrNull);
		}).mapToInt(Category::getSortOrder).max().orElse(-1) + 1;
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
}
