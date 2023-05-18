package jpabook.jpashop.controller;

import jpabook.jpashop.Service.ItemService;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.FlagSection;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new FlagSectionForm());
        return "items/createItemForm";
    }

    @PostMapping("items/new")
    public String create(FlagSectionForm form) {
        FlagSection flagSection = new FlagSection();
        flagSection.setName(form.getName());
        flagSection.setPrice(form.getPrice());
        flagSection.setStockQuantity(form.getStockQuantity());
        flagSection.setStartPlace(form.getStartPlace());
        flagSection.setEndPlace(form.getEndPlace());

        itemService.saveItem(flagSection);
        return "redirect:/";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    @GetMapping("items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {

        FlagSection item = (FlagSection) itemService.findOne(itemId);

        FlagSectionForm form = new FlagSectionForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setStartPlace(item.getStartPlace());
        form.setEndPlace(item.getEndPlace());

        model.addAttribute("form", form);

        return "items/updateItemForm";
    }

    @PostMapping("items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId, @ModelAttribute("form") FlagSectionForm form) {

        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());

        return "redirect:/items";
    }

}
