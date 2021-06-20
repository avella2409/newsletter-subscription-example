package com.avella.example.newslettersubscriptionexample.domain.subscriber;

import com.avella.example.newslettersubscriptionexample.domain.shared.Entity;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions.AlreadyHasASubscriptionToTheNewsletterException;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions.ChangeToSamePlanException;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions.NotSubscribedToTheNewsletterException;
import com.avella.example.newslettersubscriptionexample.domain.subscriber.exceptions.PlanLimitExceededException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Subscriber extends Entity {

    private final String email;
    private SubscriberPlan plan;
    private long lastSubscriptionNumber;
    private Set<Subscription> allSubscription;

    public Subscriber(UUID id, String email) {
        super(id);
        this.email = email;
        this.plan = SubscriberPlan.FREE;
        this.allSubscription = new HashSet<>();
        this.lastSubscriptionNumber = 0;
    }

    private Subscriber(SubscriberMemento memento) {
        super(memento.entityMemento());
        this.email = memento.email();
        this.plan = memento.plan();
        this.allSubscription = new HashSet<>(memento.allSubscription());
        this.lastSubscriptionNumber = memento.lastSubscriptionNumber();
    }

    public static Subscriber fromMemento(SubscriberMemento memento) {
        return new Subscriber(memento);
    }

    public SubscriberMemento createMemento() {
        return new SubscriberMemento(super.createEntityMemento(), email, plan, Set.copyOf(allSubscription),
                lastSubscriptionNumber);
    }

    public String email() {
        return email;
    }

    public void subscribeToNewsletter(UUID newsletterId) {
        if (numberOfSubscriptionIsAtPlanThreshold())
            throw new PlanLimitExceededException();
        else if (isSubscribedToNewsletter(newsletterId))
            throw new AlreadyHasASubscriptionToTheNewsletterException(newsletterId);
        allSubscription.add(new Subscription(newsletterId, ++lastSubscriptionNumber));
    }

    public void unsubscribeFromNewsletter(UUID newsletterId) {
        if (!isSubscribedToNewsletter(newsletterId))
            throw new NotSubscribedToTheNewsletterException(newsletterId);
        allSubscription.removeIf(subscription -> subscription.newsletterId().equals(newsletterId));
    }

    public void changePlan(SubscriberPlan newPlan) {
        if (plan.equals(newPlan))
            throw new ChangeToSamePlanException();
        plan = newPlan;
        removeExceedingSubscription();
    }

    private boolean isSubscribedToNewsletter(UUID newsletterId) {
        return allSubscription.stream().anyMatch(subscription -> subscription.newsletterId().equals(newsletterId));
    }

    private boolean numberOfSubscriptionIsAtPlanThreshold() {
        return allSubscription.size() == plan.subscriptionThreshold;
    }

    private void removeExceedingSubscription() {
        int maximumSubscription = plan.subscriptionThreshold;
        if (allSubscription.size() > maximumSubscription) {
            allSubscription = allSubscription.stream()
                    .sorted(Subscription.sortBySubscriptionNumberAsc())
                    .limit(maximumSubscription)
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subscriber that = (Subscriber) o;
        return lastSubscriptionNumber == that.lastSubscriptionNumber
                && Objects.equals(email, that.email) && plan == that.plan
                && Objects.equals(allSubscription, that.allSubscription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), email, plan, lastSubscriptionNumber, allSubscription);
    }

    @Override
    public String toString() {
        return "Subscriber{" +
                "email='" + email + '\'' +
                ", plan=" + plan +
                ", lastSubscriptionNumber=" + lastSubscriptionNumber +
                ", allSubscription=" + allSubscription +
                '}';
    }
}
