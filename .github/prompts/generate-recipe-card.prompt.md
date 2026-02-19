---
name: generate-recipe-card
description: 'Generate a Thymeleaf recipe card component with Spring Boot integration'
agent: 'agent'
---

Create a new Thymeleaf HTML fragment for displaying recipe cards in the FlavorHub application.
     
     Component name: ${input:componentName:recipe-card}
     Component purpose: ${input:purpose:Display a recipe with image, title, and metadata}
     Location: flavorhub/src/main/resources/templates/fragments/${input:componentName}.html
     
     Requirements:
     - Use Thymeleaf syntax (th:* attributes)
     - Include proper Spring Boot template structure
     - Make it responsive with modern CSS (flexbox/grid)
     - Add hover effects and transitions
     - Use semantic HTML5 elements
     - Include accessibility attributes (aria-labels, alt text)
     - Style with inline CSS or reference existing styles
     - Accept a recipe object as a Thymeleaf parameter
     - Follow the existing template patterns in src/main/resources/templates/
     
     The component should be reusable across different pages of the application.
     
     Example usage in a controller:
     ```java
     model.addAttribute("recipe", recipe);
     ```
     
     Example usage in a template:
     ```html
     <div th:replace="fragments/${componentName} :: ${componentName}(recipe=${recipe})"></div>
     ```