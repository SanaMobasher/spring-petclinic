package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link PetTypeFormatter}
 *
 * @author Colin But
 */
@ExtendWith(MockitoExtension.class)
@DisabledInNativeImage
class PetTypeFormatterTests {

	@Mock
	private OwnerRepository pets;

	private PetTypeFormatter petTypeFormatter;

	// Move setup logic into the constructor or directly initialize the field
	public PetTypeFormatterTests() {
		// Direct field initialization could be done here (although in most cases we use @BeforeEach)
		this.petTypeFormatter = new PetTypeFormatter(pets);
	}

	@Test
	void testPrint() {
		PetType petType = new PetType();
		petType.setName("Hamster");
		String petTypeName = this.petTypeFormatter.print(petType, Locale.ENGLISH);
		assertThat(petTypeName).isEqualTo("Hamster");
	}

	@Test
	void shouldParse() throws ParseException {
		// Here, instead of calling given in setup, directly do it in the test method
		given(this.pets.findPetTypes()).willReturn(makePetTypes());
		PetType petType = petTypeFormatter.parse("Bird", Locale.ENGLISH);
		assertThat(petType.getName()).isEqualTo("Bird");
	}

	@Test
	void shouldThrowParseException() {
		// Mocking is still done in test method
		given(this.pets.findPetTypes()).willReturn(makePetTypes());
		Assertions.assertThrows(ParseException.class, () -> {
			petTypeFormatter.parse("Fish", Locale.ENGLISH);
		});
	}

	// Refactored initialization logic using a standard method instead of in-line logic
	private List<PetType> makePetTypes() {
		List<PetType> petTypes = new ArrayList<>();
		// Added method call for clarity and consistency
		petTypes.add(createPetType("Dog"));
		petTypes.add(createPetType("Bird"));
		return petTypes;
	}

	// Moved creation logic into its own method to avoid inline initialization
	private PetType createPetType(String name) {
		PetType petType = new PetType();
		petType.setName(name);
		return petType;
	}
}
