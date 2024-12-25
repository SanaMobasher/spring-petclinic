package org.springframework.samples.petclinic.owner;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OwnerController.class)
@DisabledInAotMode
class OwnerControllerTests {

	private static final int TEST_OWNER_ID = 1;
	private static final String LAST_NAME = "lastName";
	private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 20);

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	private Owner george;

	private Owner createGeorge() {
		Owner owner = new Owner();
		owner.setId(TEST_OWNER_ID);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		owner.setAddress("110 W. Liberty St.");
		owner.setCity("Madison");
		owner.setTelephone("6085551023");

		Pet max = new Pet();
		PetType dog = new PetType();
		dog.setName("dog");
		max.setType(dog);
		max.setName("Max");
		max.setBirthDate(LocalDate.now());
		owner.addPet(max);
		max.setId(1);

		return owner;
	}

	private Page<Owner> createOwnersPage(Owner... owners) {
		return new PageImpl<>(Lists.newArrayList(owners));
	}

	@BeforeEach
	void setup() {
		george = createGeorge();

		// Mock repository behavior
		given(owners.findByLastNameStartingWith("Franklin", DEFAULT_PAGEABLE))
			.willReturn(createOwnersPage(george));
		given(owners.findAll(DEFAULT_PAGEABLE))
			.willReturn(createOwnersPage(george));
		given(owners.findById(TEST_OWNER_ID))
			.willReturn(Optional.of(george));

		Visit visit = new Visit();
		visit.setDate(LocalDate.now());
		george.getPet("Max").getVisits().add(visit);
	}

	@Test
	void testInitFindForm() throws Exception {
		mockMvc.perform(get("/owners/find"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("owner"))
			.andExpect(view().name("owners/findOwners"));
	}

	@Test
	void testProcessFindFormByLastName() throws Exception {
		when(owners.findByLastNameStartingWith(ArgumentMatchers.eq("Franklin"), ArgumentMatchers.any(Pageable.class)))
			.thenReturn(createOwnersPage(george));

		mockMvc.perform(get("/owners?page=1").param(LAST_NAME, "Franklin"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/" + TEST_OWNER_ID));
	}

	@Test
	void testProcessFindFormNoOwnersFound() throws Exception {
		when(owners.findByLastNameStartingWith(ArgumentMatchers.eq("Unknown Surname"), ArgumentMatchers.any(Pageable.class)))
			.thenReturn(createOwnersPage());

		mockMvc.perform(get("/owners?page=1").param(LAST_NAME, "Unknown Surname"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("owner", LAST_NAME))
			.andExpect(model().attributeHasFieldErrorCode("owner", LAST_NAME, "notFound"))
			.andExpect(view().name("owners/findOwners"));
	}

	@Test
	void testProcessUpdateOwnerFormWithIdMismatch() throws Exception {
		int pathOwnerId = TEST_OWNER_ID;

		Owner mismatchedOwner = new Owner();
		mismatchedOwner.setId(2);
		mismatchedOwner.setFirstName("John");
		mismatchedOwner.setLastName("Doe");
		mismatchedOwner.setAddress("Center Street");
		mismatchedOwner.setCity("New York");
		mismatchedOwner.setTelephone("0123456789");

		when(owners.findById(pathOwnerId)).thenReturn(Optional.of(george));

		mockMvc.perform(post("/owners/{ownerId}/edit", pathOwnerId).flashAttr("owner", mismatchedOwner))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners/" + pathOwnerId + "/edit"))
			.andExpect(flash().attributeExists("error"));
	}

	@Test
	void testShowOwner() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}", TEST_OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
			.andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
			.andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
			.andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
			.andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
			.andExpect(model().attribute("owner", hasProperty("pets", not(empty()))))
			.andExpect(view().name("owners/ownerDetails"));
	}
}
