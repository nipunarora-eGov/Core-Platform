import React from "react";
import { SubdirectoryArrowLeft } from "./SubdirectoryArrowLeft";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SubdirectoryArrowLeft",
  component: SubdirectoryArrowLeft,
};

export const Default = () => <SubdirectoryArrowLeft />;
export const Fill = () => <SubdirectoryArrowLeft fill="blue" />;
export const Size = () => <SubdirectoryArrowLeft height="50" width="50" />;
export const CustomStyle = () => <SubdirectoryArrowLeft style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SubdirectoryArrowLeft className="custom-class" />;
