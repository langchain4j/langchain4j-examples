import {LitElement, html, css} from 'lit';
import '@vaadin/icon';
import '@vaadin/button';
import '@vaadin/text-field';
import '@vaadin/text-area';
import '@vaadin/form-layout';
import '@vaadin/progress-bar';
import '@vaadin/checkbox';
import '@vaadin/grid';
import '@vaadin/grid/vaadin-grid-sort-column.js';

export class DemoTitle extends LitElement {

    static styles = css`
      h1 {
        font-family: "Red Hat Mono", monospace;
        font-size: 60px;
        font-style: normal;
        font-variant: normal;
        font-weight: 700;
        line-height: 26.4px;
        color: var(--main-highlight-text-color);
      }

      .title {
        text-align: center;
        padding: 1em;
        background: var(--main-bg-color);
      }
      
      .explanation {
        margin-left: auto;
        margin-right: auto;
        width: 50%;
        text-align: justify;
        font-size: 20px;
      }
    `

    render() {
        return html`
            <div class="title">
                <h1>Cash Critique</h1>
            </div>
            <div class="explanation">
                This demo shows how an LLM can triage reviews automatically.
                This frontend lets you write a review about a financial product and submit it to the backend.
                The backend use LLM (GPT 3.5) to evaluate the review and provide a response.
            </div>
        `
    }


}

customElements.define('demo-title', DemoTitle);