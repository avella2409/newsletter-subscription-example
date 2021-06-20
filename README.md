### Goal

The goal of this `example project` is to show how I create most of my project.
What I wanted to show:

- How I apply the `clean architecture` (Hexagonal / Onion / Ports and Adapters)
- How I use `reactive programming`
- How I apply `domain driven design`
- My `testing strategy` (Unit/Integration/E2E)
- How I use the `CQRS pattern` (for this example just at code level)

### Project

To demonstrate what I wanted to show I needed to invent some requirements, I decided to go for a `Newsletter subscription` service with simplified rules.

Here are the spec:

- User can `subscribe to newsletter`
- User can `unsubscribe from newsletter`
- User can `see all of his subscription`
- User with `free plan` can subscribe to `5 newsletter maximum`
- User with `premium plan` can subscribe to `30 newsletter maximum`
- When a user change plan and go `from premium to free` and has more than 5 subscriptions he loses all subscription above free plan threshold and so `goes back to the first 5 subscription` he made
- When a `new newsletter is published` every subscriber of this newsletter is `notified by an email`

This service is `focusing only on user subscription` so all other aspects are supposedly `made by others services`.

When a user registers the imaginary service handling this requirement is `sending an event`, and this project catch it to instantiate a `Subscriber` on our side. Same applies when a user is changing his plan, we only catch the event saying the user has changed his plan.

Same goes for authentication, when a user subscribes/unsubscribes or wants to see all of his subscription, there is no authentication on our side, it's made by an imaginary service forwarding the request to our service after the authentication of the user.

### Disclaimer

I did this project in approximately 8 hours, so everything is not perfect, but it was not the goal. The only goal was to show briefly how I apply `architecture` and `coding principle` on a simple project.

### Technology used

Java 16, Spring, Reactor, Reactive RabbitMQ, R2DBC PostgreSQL, TestContainers, SendGrid
