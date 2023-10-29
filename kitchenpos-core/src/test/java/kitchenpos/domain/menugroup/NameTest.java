package kitchenpos.domain.menugroup;

import kitchenpos.exception.KitchenposException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static kitchenpos.exception.ExceptionInformation.MENU_GROUP_NAME_IS_NULL;
import static kitchenpos.exception.ExceptionInformation.MENU_GROUP_NAME_LENGTH_OUT_OF_BOUNCE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("메뉴그룹 이름 테스트")
class NameTest {

    @ParameterizedTest
    @ValueSource(strings = {"가", "가나다라마바사아자차카타파하까나따라마빠"})
    void 이름_정상_생성(String name) {
        assertDoesNotThrow(() -> Name.create(name));
    }

    @Test
    void 메뉴그룹_이름은_비어있을수_없다() {
        assertThatThrownBy(() -> Name.create(null))
                .isExactlyInstanceOf(KitchenposException.class)
                .hasMessage(MENU_GROUP_NAME_IS_NULL.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void 메뉴그룹_이름은_빈칸일_수_없다(String name) {
        assertThatThrownBy(() -> Name.create(name))
                .isExactlyInstanceOf(KitchenposException.class)
                .hasMessage(MENU_GROUP_NAME_LENGTH_OUT_OF_BOUNCE.getMessage());
    }

    @Test
    void 메뉴그룹_이름은_20글자_이하여야한다() {
        assertThatThrownBy(() -> Name.create("가나다라마바사아자차카타파하까나따라마빠하"))
                .isExactlyInstanceOf(KitchenposException.class)
                .hasMessage(MENU_GROUP_NAME_LENGTH_OUT_OF_BOUNCE.getMessage());
    }

}