package delft;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.util.*;
import java.util.stream.*;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static delft.Field.*;
import static delft.Property.*;
import static delft.SportsHallPlanner.planHalls;

class SportsHallPlannerTests {
    // ---------------- Spec-based ----------------

    @Test
    void emptyRequests_returnsEmptyMapping_evenIfHallsPresent() {
        SportsHall h1 = new SportsHall(
                Set.of(NEAR_CITY_CENTRE),
                Map.of(BASKETBALL, 2)
        );

        Map<SportsHall, Request> result = planHalls(emptyList(), List.of(h1));

        assertThat(result).isEmpty();
    }

    @Test
    void oneRequest_multipleHalls_pickFirstThatCanFulfill() {
        Request r = new Request(Set.of(HAS_RESTAURANT), TENNIS, 2);

        SportsHall bad = new SportsHall(Set.of(HAS_RESTAURANT), Map.of(TENNIS, 1)); // pas assez de terrains
        SportsHall okFirst = new SportsHall(Set.of(HAS_RESTAURANT), Map.of(TENNIS, 2));
        SportsHall okSecond = new SportsHall(Set.of(HAS_RESTAURANT, NEAR_CITY_CENTRE), Map.of(TENNIS, 3));

        Map<SportsHall, Request> result = planHalls(List.of(r), List.of(bad, okFirst, okSecond));

        assertThat(result).hasSize(1).containsEntry(okFirst, r);
    }

    @Test
    void propertiesMustAllBePresent_andFieldCountAtLeastMin() {
        Request r = new Request(Set.of(NEAR_CITY_CENTRE, CLOSE_PUBLIC_TRANSPORT), VOLLEYBALL, 3);

        SportsHall missingProperty = new SportsHall(Set.of(NEAR_CITY_CENTRE), Map.of(VOLLEYBALL, 5));
        SportsHall notEnoughFields = new SportsHall(Set.of(NEAR_CITY_CENTRE, CLOSE_PUBLIC_TRANSPORT), Map.of(VOLLEYBALL, 2));
        SportsHall ok = new SportsHall(Set.of(NEAR_CITY_CENTRE, CLOSE_PUBLIC_TRANSPORT), Map.of(VOLLEYBALL, 3));

        Map<SportsHall, Request> result = planHalls(List.of(r), List.of(missingProperty, notEnoughFields, ok));

        assertThat(result).hasSize(1).containsEntry(ok, r);
    }

    @Test
    void multipleRequests_bothAssignableToMultipleHalls_firstRequestToFirstHall_secondToSecond() {
        Request r1 = new Request(Set.of(NEAR_CITY_CENTRE), BADMINTON, 1);
        Request r2 = new Request(Set.of(CLOSE_PUBLIC_TRANSPORT), BASKETBALL, 1);

        SportsHall h1 = new SportsHall(Set.of(NEAR_CITY_CENTRE, CLOSE_PUBLIC_TRANSPORT),
                Map.of(BADMINTON, 2, BASKETBALL, 1));
        SportsHall h2 = new SportsHall(Set.of(NEAR_CITY_CENTRE, CLOSE_PUBLIC_TRANSPORT),
                Map.of(BADMINTON, 1, BASKETBALL, 2));

        // Les deux salles peuvent satisfaire les deux demandes ; on attend r1->h1, r2->h2
        Map<SportsHall, Request> result = planHalls(List.of(r1, r2), List.of(h1, h2));

        assertThat(result).hasSize(2)
                .containsEntry(h1, r1)
                .containsEntry(h2, r2);
    }

    @Test
    void backtracking_whenGreedyChoiceWouldFail_findsCrossAssignment() {
        // h1 satisfait uniquement r2 ; h2 satisfait uniquement r1
        Request r1 = new Request(Set.of(HAS_RESTAURANT), TENNIS, 1);
        Request r2 = new Request(Set.of(NEAR_CITY_CENTRE), VOLLEYBALL, 1);

        SportsHall h1 = new SportsHall(Set.of(NEAR_CITY_CENTRE), Map.of(VOLLEYBALL, 1));
        SportsHall h2 = new SportsHall(Set.of(HAS_RESTAURANT), Map.of(TENNIS, 1));

        Map<SportsHall, Request> result = planHalls(List.of(r1, r2), List.of(h1, h2));

        assertThat(result).hasSize(2)
                .containsEntry(h2, r1)
                .containsEntry(h1, r2);
    }

    @Test
    void hallsCanRemainUnused_whenFewerRequestsThanHalls() {
        Request r = new Request(Set.of(), BASKETBALL, 1);

        SportsHall used = new SportsHall(Set.of(), Map.of(BASKETBALL, 1));
        SportsHall unused = new SportsHall(Set.of(HAS_RESTAURANT), Map.of(TENNIS, 10));

        Map<SportsHall, Request> result = planHalls(List.of(r), List.of(used, unused));

        assertThat(result).hasSize(1)
                .containsEntry(used, r)
                .doesNotContainKey(unused);
    }

    // ---------------- Structural / negative ----------------

    @Test
    void duplicateHalls_throwIllegalArgumentException_evenIfRequestsEmpty() {
        SportsHall a1 = new SportsHall(Set.of(NEAR_CITY_CENTRE), Map.of(BADMINTON, 1));
        SportsHall a2 = new SportsHall(Set.of(NEAR_CITY_CENTRE), Map.of(BADMINTON, 1)); // equals() => doublon

        Executable call = () -> planHalls(emptyList(), List.of(a1, a2));

        assertThatThrownBy(call)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no duplicate elements");
    }

    @Test
    void duplicateHalls_throwIllegalArgumentException_withRequestsPresent() {
        Request r = new Request(Set.of(NEAR_CITY_CENTRE), BADMINTON, 1);
        SportsHall a1 = new SportsHall(Set.of(NEAR_CITY_CENTRE), Map.of(BADMINTON, 1));
        SportsHall a2 = new SportsHall(Set.of(NEAR_CITY_CENTRE), Map.of(BADMINTON, 1)); // doublon

        assertThatThrownBy(() -> planHalls(List.of(r), List.of(a1, a2)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void noHalls_returnsNull_whenRequestsNonEmpty() {
        Request r = new Request(Set.of(), BASKETBALL, 1);

        Map<SportsHall, Request> result = planHalls(List.of(r), emptyList());

        assertThat(result).isNull();
    }

    @Test
    void returnsNull_whenNotAllRequestsCanBeAssigned() {
        Request r1 = new Request(Set.of(), TENNIS, 1);
        Request r2 = new Request(Set.of(), BASKETBALL, 1);

        SportsHall onlyForR1 = new SportsHall(Set.of(), Map.of(TENNIS, 1));

        Map<SportsHall, Request> result = planHalls(List.of(r1, r2), singletonList(onlyForR1));

        assertThat(result).isNull();
    }

    @Test
    void fieldTypeMustExist_otherwiseCannotFulfill() {
        Request r = new Request(Set.of(), VOLLEYBALL, 1);
        SportsHall h = new SportsHall(Set.of(NEAR_CITY_CENTRE, CLOSE_PUBLIC_TRANSPORT), Map.of(TENNIS, 5));

        Map<SportsHall, Request> result = planHalls(List.of(r), List.of(h));

        assertThat(result).isNull();
    }

    @Test
    void boundary_exactMinNumberOfFields_isAccepted() {
        Request r = new Request(Set.of(), BADMINTON, 3);
        SportsHall h = new SportsHall(Set.of(), Map.of(BADMINTON, 3));

        Map<SportsHall, Request> result = planHalls(List.of(r), List.of(h));

        assertThat(result).hasSize(1).containsEntry(h, r);
    }
}
