import {css, html, LitElement} from 'lit';
import '@vaadin/icon';
import '@vaadin/button';
import '@vaadin/text-field';
import '@vaadin/text-area';
import '@vaadin/form-layout';
import '@vaadin/progress-bar';
import '@vaadin/checkbox';
import '@vaadin/horizontal-layout';
import '@vaadin/grid';
import '@vaadin/grid/vaadin-grid-sort-column.js';

/**
 * Contains a field to enter the review, the submit button and the message from the backend.
 */
export class DemoReview extends LitElement {
    static styles = css`
      .button {
        cursor: pointer;
      }

      vaadin-text-area {
        width: 35em;
        height: 10rem;
      }

      vaadin-button {
        margin-left: 1em;
      }

      .outcome {
        margin-top: 1em;
        width: 100%;
      }
    `;

    static properties = {
        last_message: String,
        review: String,
        in_progress: {state: true, type: Boolean},
        complete: {state: true, type: Boolean}
    }

    constructor() {
        super();
        this.review = "";
        this.last_message = "";
        this.in_progress = false;
        this.complete = false;
    }

    render() {
        let outcome = "";
        if (this.in_progress) {
            outcome = html`
                <div class="outcome">
                    <div>Analyzing your review...</div>
                    <vaadin-progress-bar indeterminate theme="contrast"></vaadin-progress-bar>
                </div>`;
        } else if (this.complete) {
            outcome = html`
                <div class="outcome">
                    <div>${this.last_message}</div>
                </div>`;
        } else {
            outcome = html`
                <div class="outcome"></div>`;
        }

        return html`
            <vaadin-horizontal-layout theme="spacing padding"
                                      style="align-items: baseline">
            ${outcome}
            </vaadin-horizontal-layout>
            <vaadin-horizontal-layout theme="spacing padding"
                                      style="align-items: baseline">
                <vaadin-text-area
                        label="Write your review:"
                        .maxlength=1024
                        .helperText="${`${this.review.length}/1024`}"
                        .value=${this.review}
                        @value-changed="${event => {
                            this.review = event.detail.value;
                        }}"
                >
                </vaadin-text-area>
                <vaadin-button
                        arial-label="Submit your review"
                        @click=${this._submit} class="button primary">
                    Submit
                </vaadin-button>
            </vaadin-horizontal-layout>`;
    }

    _submit() {
        this.last_message = "";
        this.in_progress = true;
        this.complete = false;
        fetch(`/review`, {
            method: "POST",
            body: JSON.stringify({
                review: this.review
            }),
            headers: {
                "Content-Type": "application/json",
            },
        }).then(response => response.json())
            .then(data => {
                this.in_progress = false;
                this.complete = true;
                this.review = "";
                this.last_message = data.message;
            })
            .catch((error) => {
                console.error('Error:', error);
            });
    }

}

customElements.define('demo-review', DemoReview);